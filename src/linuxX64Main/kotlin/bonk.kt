import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import kotlinx.cinterop.*
import logging.getLogger
import magickwand.*
import platform.posix.size_tVar

class WandException(wand: CPointer<MagickWand>, operation: String) : RuntimeException() {
    override val message: String = memScoped {
        val severity = alloc<ExceptionTypeVar>()
        val description: String? = MagickGetException(wand, severity.ptr)?.toKStringFromUtf8()

        "$description ${severity.value} failed in operation: $operation"
    }
}

object BonkMagick {
    private val logger = getLogger()

    fun MagickBooleanType.checkStatus(
        magicWand: CPointer<MagickWand>,
        operation: String,
        throwOnFailure: Boolean = true
    ): Boolean {
        val success = this == MagickTrue
        return if (!success) {
            logger.error { "success of $operation: $success" }
            if (throwOnFailure) {
                throw WandException(magicWand, operation)
            } else {
                false
            }
        } else {
            success
        }
    }

    fun MemScope.bonk(imagePath: String, text: String): Memory {
//        $image = new Imagick();
//        $image->readImage("bonk.jpg");
//
//        $draw = new ImagickDraw();
//        $draw->setFillColor('black');
//        $draw->setGravity(imagick::GRAVITY_NORTH);
//        $draw->setFont('Open-Sans-SemiBold');
//
//
//        header('Content-type: image/jpeg');
//        echo $image;

        val magickWand: CPointer<MagickWand> = NewMagickWand() ?: error("failed to create MagickWand")
        MagickReadImage(magickWand, imagePath).checkStatus(magickWand, "readImage")

        logger.debug {
            val height = MagickGetImageHeight(magickWand)
            val width = MagickGetImageWidth(magickWand)
            "image: ${width.toLong()} x ${height.toLong()}"
        }

        logger.info { "annotating with text: $text" }

        val drawingWand = NewDrawingWand()
        val pixelWand = NewPixelWand()
        PixelSetColor(pixelWand, "black").checkStatus(magickWand, "setColor")
        DrawSetFillColor(drawingWand, pixelWand)
        DrawSetGravity(drawingWand, NorthGravity)
        DrawSetFont(drawingWand, "Open-Sans-SemiBold").checkStatus(magickWand, "setFont")
//        DrawSetFont(drawingWand, "data/OpenSans-SemiBold.ttf").checkStatus(magickWand, "setFont")
        DrawSetFontSize(drawingWand, 60.0)
        MagickAnnotateImage(magickWand, drawingWand, 0.0, 12.0, 0.0, text)
            .checkStatus(magickWand, "annotateImage")

        MagickSetImageFormat(magickWand, "jpg")

        val length = alloc<size_tVar>()
        val blob: CPointer<UByteVarOf<UByte>> = MagickGetImageBlob(magickWand, length.ptr) ?: error("failed to write blob")
        logger.debug { "written ${length.value} bytes" }

        blob as CPointer<ByteVar>
        val memory = allocMemory(length.value.toLong())
        blob.copyTo(memory, 0, length.value.toLong(), 0)
        MagickRelinquishMemory(blob)

//        MagickWriteImages(magicWand,"out.jpg",MagickTrue).checkStatus(magicWand, "writeImageToFile")

        DestroyPixelWand(pixelWand)
        DestroyDrawingWand(drawingWand)
        DestroyMagickWand(magickWand)
        MagickWandTerminus()

        return memory
    }
}