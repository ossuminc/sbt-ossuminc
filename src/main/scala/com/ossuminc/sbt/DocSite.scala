package com.ossuminc.sbt

import com.ossuminc.sbt.OssumIncPlugin.autoImport.With
import sbt.{File, Project, ProjectReference, file}

/** A [[DocSite]] is a sub-directory that aims at providing documentation
 * with the ScalaDoc integrated into its contents via the unidoc plugin.
 * @param dirName
 *   The directory from repository root where the documentation will live
 * @param apiOutput
 *   Where the unidoc plugin should be placed in the documentation. It
 *   should be a subdirectory name of dirName
 * @param baseURL
 *   The URL to use as the base of the documentation. Should be where your website shows up
 * @param inclusions
 *   A list of projects in your build that should be included in the documentation
 * @param exclusions
 *   A list of projects in your build that should be excluded from the documentation
 * @param logoPath
 *   The path to your documentations logo, relative to dirName. Defaults to the scala logo
 * @param externalMappings
 *  A list of string tuples for the scaladoc argument --external-mappings
 */
object DocSite {

  /** Apply the DocSite  */
  def apply(
    dirName: String,
    apiOutput: File,
    baseURL: Option[String] = None,
    inclusions: Seq[ProjectReference] = Seq.empty,
    exclusions: Seq[ProjectReference] = Seq.empty,
    logoPath: Option[String] = None,
    externalMappings: Seq[Seq[String]] = Seq.empty
  ): Project = {
    val outpath = file(dirName ++ "/" ++ apiOutput.toString)
    Project
      .apply(dirName, file(dirName))
      .enablePlugins(OssumIncPlugin)
      .configure(With.basic, With.scala3)
      .configure(With.unidoc(outpath, baseURL, inclusions, exclusions, logoPath, externalMappings))
  }
}
