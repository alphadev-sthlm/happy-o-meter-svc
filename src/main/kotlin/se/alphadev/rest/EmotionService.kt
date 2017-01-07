package se.alphadev.rest

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.bouncycastle.asn1.cms.CMSAttributes.contentType
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import se.alphadev.image.Face
import se.alphadev.image.MemeEmotionRenderer
import se.alphadev.image.Rect
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class EmotionService {
    val client = OkHttpClient()

    val LOG = LoggerFactory.getLogger("EmotionService")

    @Autowired
    lateinit var renderer: MemeEmotionRenderer

    @Value("\${emo-api.url}")
    var emoUrl: String = ""

    @RequestMapping("/emotions", method = arrayOf(RequestMethod.POST))
    fun emotions(req: HttpServletRequest, resp: HttpServletResponse) {

        val contentType = req.getHeader("content-type")
        val size = Integer.parseInt(req.getHeader("content-length"))

        val imgBytes = readImageData(contentType, req.inputStream, size)


        val faces = parseFaces( emotionDoc(contentType, imgBytes) )

        val newImage = renderer.render(imgBytes, faces, req.locale)

        resp.addHeader("content-type", newImage.second.mimeType)
        resp.outputStream.write(newImage.first)
    }

    fun emotionDoc(contentType: String, imgBytes: ByteArray): String {
        val emoReq = Request.Builder()
                .url(emoUrl)
                .post(RequestBody.create(MediaType.parse(contentType), imgBytes))
                .build()
        return client.newCall(emoReq).execute().body().string()
    }

    private fun parseFaces(json: String): List<Face> {
        val jsonFaces = JSONTokener(json).nextValue()
        if (jsonFaces !is JSONArray) {
            return listOf()
        }

        val faces = arrayListOf<Face>()

        for (jsonFace in jsonFaces) {
            if (jsonFace !is JSONObject) {
                continue
            }

            val faceRect = jsonFace.getJSONObject("faceRectangle")
            val x = faceRect.getInt("left")
            val y = faceRect.getInt("top")
            val w = faceRect.getInt("width")
            val h = faceRect.getInt("height")

            val jsonScores = jsonFace.getJSONObject("scores")
            val scores = arrayListOf<Pair<String, Double>>()

            for (emo in jsonScores.keys()) {
                scores.add(Pair(emo, jsonScores.getDouble(emo)))
            }

            faces.add(Face(scores, Rect(x, y, w, h)))
        }

        return faces
    }

    private fun readImageData(contentType: String, input: InputStream, size: Int): ByteArray {
        val bytes = input.readBytes(size)

        if (contentType.endsWith(";base64")) {
            return Base64.getDecoder().decode(bytes)
        }

        return bytes
    }
}
