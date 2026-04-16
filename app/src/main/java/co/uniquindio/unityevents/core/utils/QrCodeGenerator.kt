package co.uniquindio.unityevents.core.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Generador de codigos QR usando ZXing. Devuelve un [Bitmap] en memoria que Compose
 * dibuja via [Image]. El tamano recomendado es 512..1024 px para legibilidad.
 */
object QrCodeGenerator {

    /**
     * Codifica [content] en un QR blanco y negro de [sizePx] x [sizePx].
     *
     * @param content payload (en UnityEvents, es el id del ticket).
     * @param sizePx tamano en pixeles del bitmap resultante.
     */
    fun generate(content: String, sizePx: Int = 720): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /** Version Compose-friendly: regresa un [ImageBitmap] listo para `Image(bitmap = ...)`. */
    fun generateCompose(content: String, sizePx: Int = 720): ImageBitmap =
        generate(content, sizePx).asImageBitmap()
}
