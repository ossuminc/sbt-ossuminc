package com.ossuminc.sbt

import _root_.sbt.internal.util.ManagedLogger

import java.io.{File, InputStream}
import java.net.{HttpURLConnection, URI}
import scala.io.Source

package object helpers {

  def updateFromPublicRepository(
    local: File,
    etagFile: File,
    remote: String, // start with github organization or user
    log: ManagedLogger
  ): Unit = {
    val url = URI.create(s"https://raw.githubusercontent.com/$remote").toURL
    val conn: HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
    val timeout = 30000
    conn.setConnectTimeout(timeout)
    conn.setReadTimeout(timeout)
    conn.setInstanceFollowRedirects(true)
    conn.connect()
    val status = conn.getResponseCode
    val message = conn.getResponseMessage
    val etag = conn.getHeaderField("ETag")
    val last_etag = {
      if (etagFile.exists()) {
        Source.fromFile(etagFile).mkString
      } else { "" }
    }
    if (etag != last_etag) {
      import java.nio.file.Files
      import java.nio.charset.StandardCharsets
      Files.write(etagFile.toPath, etag.getBytes(StandardCharsets.UTF_8))
      status match {
        case java.net.HttpURLConnection.HTTP_OK =>
          Option(conn.getContent()) match {
            case Some(is: InputStream) =>
              val content = Source.fromInputStream(is).mkString
              val pw = new java.io.PrintWriter(local)
              try {
                pw.write(content)
              } finally {
                pw.close()
              }
              log.info(
                s"Updated .scalafmt.conf: HTTP $status $message ETag($etag)"
              )
            case Some(x: Any) =>
              throw new IllegalStateException(
                s"Wrong content type for ${url.toExternalForm}: ${x.getClass}."
              )
            case None =>
              throw new IllegalStateException(
                s"No content from ${url.toExternalForm}"
              )
          }
        case java.net.HttpURLConnection.HTTP_NOT_MODIFIED =>
          log.info(s"Remote (${url.toExternalForm}) is not modified: HTTP $status: $message")
        case x: Int =>
          log.warn(
            s"Failed to get ${url.toExternalForm}: HTTP $status: $message"
          )
      }
    } else {
      log.info(s"Didn't download because remote etag($etag) == local etag($last_etag)")
    }
  }
}
