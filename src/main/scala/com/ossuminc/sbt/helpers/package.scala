package com.ossuminc.sbt

import _root_.sbt.internal.util.ManagedLogger

import java.io.{File, InputStream}
import java.net.{HttpURLConnection, URI}
import java.time.Instant
import scala.concurrent.duration.FiniteDuration
import scala.io.Source

package object helpers {

  def updateFromPublicRepository(
    local: File,
    remote: String, // start with github organization or user
    maxAge: FiniteDuration,
    log: ManagedLogger
  ): Unit = {
    val url = URI.create(s"https://raw.githubusercontent.com/$remote").toURL
    val lastModified = local.lastModified()

    val conn: HttpURLConnection =
      url.openConnection().asInstanceOf[HttpURLConnection]
    val timeout = 30000
    conn.setConnectTimeout(timeout)
    conn.setReadTimeout(timeout)
    conn.setInstanceFollowRedirects(true)
    conn.setIfModifiedSince(lastModified)
    conn.connect()
    val status = conn.getResponseCode
    val message = conn.getResponseMessage

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
              s"Updated .scalafmt.conf from ${url.toExternalForm}: HTTP $status $message ${Instant.ofEpochMilli(lastModified)}"
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
      // hasn't been modified, so do nothing
      case x: Int =>
        log.warn(
          s"Failed to get ${url.toExternalForm}: HTTP $status: $message"
        )
    }
  }

}
