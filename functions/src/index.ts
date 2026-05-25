/**
 * Cloud Functions de UnityEvents.
 *
 * Escucha cuando se crea un documento en /users/{userId}/notifications/{notifId}
 * y envia una push notification (FCM) a TODOS los dispositivos del usuario
 * registrados en su array `fcmTokens`.
 *
 * El cliente Android ya guarda automaticamente el token en ese array (ver
 * UnityEventsFcmService.kt y ProfileRepositoryImpl.saveFcmToken). Asi, este
 * trigger es lo unico que hay que mantener server-side.
 *
 * Para desplegar:  npm run deploy   (o firebase deploy --only functions)
 */

import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Trigger principal: cuando aparece una notificacion nueva en la bandeja de un usuario,
 * leemos sus FCM tokens y le mandamos el push.
 *
 * Region: us-central1 (default). No se cambia para mantener compatibilidad con el
 * proyecto y aprovechar el free tier.
 */
export const sendPushOnNewNotification = onDocumentCreated(
  "users/{userId}/notifications/{notifId}",
  async (event) => {
    const userId = event.params.userId;
    const notif = event.data?.data();
    if (!notif) {
      logger.warn("Documento sin datos, ignorando", { userId });
      return;
    }

    // Lee los tokens del usuario destinatario.
    const userSnap = await admin
      .firestore()
      .doc(`users/${userId}`)
      .get();
    const tokens: string[] = (userSnap.get("fcmTokens") as string[]) || [];

    if (tokens.length === 0) {
      logger.info("Usuario sin FCM tokens registrados, no se envia push", { userId });
      return;
    }

    // Arma el payload. Mandamos titulo + body desde el documento de notificacion.
    // Tambien pasamos `type` y `relatedId` en data para que la app pueda hacer deep-link
    // al toque (ej. abrir el evento relacionado).
    const message: admin.messaging.MulticastMessage = {
      tokens,
      notification: {
        title: (notif.title as string) || "UnityEvents",
        body: (notif.body as string) || "",
      },
      data: {
        type: (notif.type as string) || "INFO",
        relatedId: (notif.relatedId as string) || "",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "unityevents_default",
        },
      },
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    logger.info("Push enviado", {
      userId,
      total: tokens.length,
      exitos: response.successCount,
      fallos: response.failureCount,
    });

    // Limpieza: si Firebase reporta que un token es invalido (app desinstalada,
    // token rotado, etc.), lo quitamos del array para no volverlo a usar.
    const invalidTokens: string[] = [];
    response.responses.forEach((res, idx) => {
      if (!res.success) {
        const code = res.error?.code;
        if (
          code === "messaging/invalid-registration-token" ||
          code === "messaging/registration-token-not-registered"
        ) {
          invalidTokens.push(tokens[idx]);
        }
      }
    });
    if (invalidTokens.length > 0) {
      logger.info("Limpiando tokens invalidos", { userId, count: invalidTokens.length });
      await admin
        .firestore()
        .doc(`users/${userId}`)
        .update({
          fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidTokens),
        });
    }
  }
);
