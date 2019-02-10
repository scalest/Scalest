package scalest.admin

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

package object pagination {
  case class PageRequest(size: Int = PageRequest.DefaultPageSize, page: Int = PageRequest.DefaultPage) {
    val offset: Int = page * size
  }

  object PageRequest {
    val DefaultPageSize = 20
    val DefaultPage = 0
    val DefaultPageRequest = PageRequest(DefaultPageSize, DefaultPage)

    implicit val pageRequestEncoder: Encoder[PageRequest] = deriveEncoder[PageRequest]
    implicit val pageRequestDecoder: Decoder[PageRequest] = deriveDecoder[PageRequest]
  }

  case class PageResponse[T](
    size: Int,
    number: Int,
    totalElements: Int,
    totalPages: Int,
    first: Boolean,
    last: Boolean,
    numberOfElements: Int,
    content: Seq[T],
  )

  object PageResponse {
    implicit def pageResponseEncoder[T: Encoder]: Encoder[PageResponse[T]] = deriveEncoder[PageResponse[T]]

    implicit def pageResponseDecoder[T: Decoder]: Decoder[PageResponse[T]] = deriveDecoder[PageResponse[T]]

    def response[T](content: Seq[T], total: Int, request: PageRequest): PageResponse[T] = {
      val totalPages = math.ceil(total.toDouble / request.size.toDouble).toInt

      PageResponse(
        size = request.size,
        number = request.page,
        totalElements = total,
        totalPages = totalPages,
        first = request.page == 0,
        last = request.page == totalPages - 1,
        numberOfElements = content.size,
        content = content,
      )
    }
  }

}
