package com.ossuminc.sbt.helpers

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.Keys.*
import sbt.internal.util.ManagedLogger
import sbt.{Project, *}

import java.io.{File, InputStream}
import java.net.{HttpURLConnection, URI}
import scala.io.Source

object Scalafmt extends AutoPluginHelper {

  object Keys {
    val putScalafmtConfETagsIn: SettingKey[File] =
      settingKey[File]("File path in local repo to store .scalafmt.conf.etag value")

  }

  private val scalafmt_path: String = "/ossuminc/sbt-ossuminc/main/.scalafmt.conf"
  private val scalafmt_conf: File = file(System.getProperty("user.dir")) / ".scalafmt.conf"
  private val scalafmt_config_etag_path: File = file(System.getProperty("user.dir")) / ".scalafmt.conf.etag"

  def configure(project: Project): Project = {
    project
      .enablePlugins(ScalafmtPlugin)
      .settings(
        Keys.putScalafmtConfETagsIn := scalafmt_config_etag_path,
        update := {
          val log = streams.value.log
          updateFromPublicRepository(scalafmt_conf, Keys.putScalafmtConfETagsIn.value, scalafmt_path, log)
          update.value
        },
        cleanFiles += scalafmt_config_etag_path
      )
  }

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
      import java.nio.charset.StandardCharsets
      import java.nio.file.Files
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
      // log.info(s"Didn't download because remote etag($etag) == local etag($last_etag)")
    }
  }
}
