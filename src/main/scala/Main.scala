import com.twitter.finagle.Service
import com.twitter.finagle.http.{Status, Response, Request, RequestBuilder}
import com.twitter.io.{Charsets, Buf}
import com.twitter.util.{Future, Await}
import io.finch._
// // Uncomment for finch < 0.9.2
// import io.finch.request._

object Main extends App {
  val theString = "{\"foos\": [1, 2]}"

  def printMessage(str: String): Unit = {
    if (str != theString) {
      println(s"Got the wrong string: '$str'")
    } else {
      println("OK!")
    }
  }

  val finagleImpl = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      Future.value {
        printMessage(request.getContentString())
        Response(Status.Ok)
      }
    }
  }

  val endpoint = post("foo" ? body) { str: String =>
    printMessage(str)
    Ok("")
  }
  val service = endpoint.toService

  val buf1 = Buf.Utf8(theString)
  val buf2 = Buf.ByteArray.Owned(theString.getBytes(Charsets.Utf8))
  val request1 = RequestBuilder().url("http://localhost:8080/foo").buildPost(buf1)
  val request2 = RequestBuilder().url("http://localhost:8080/foo").buildPost(buf2)

  println("**** Raw finagle-http ***")
  Await.all(finagleImpl(request1), finagleImpl(request2))

  println("**** finch ***")
  Await.all(service(request1), service(request2))
}
