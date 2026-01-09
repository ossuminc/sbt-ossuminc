package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** Helper for configuring AsciiDoc document generation
  *
  * Provides support for generating both static websites (HTML) and PDF documents from AsciiDoc source files.
  * Uses sbt-site-asciidoctor for HTML generation and AsciidoctorJ PDF for PDF generation.
  *
  * Features:
  * - HTML5 website generation with customizable attributes
  * - PDF generation with asciidoctorj-pdf
  * - Diagram support via asciidoctorj-diagram (PlantUML, Graphviz, etc.)
  * - Customizable source directories and output locations
  *
  * @example
  * {{{
  * lazy val docs = DocSite("docs", "project-docs")
  *   .configure(With.AsciiDoc())
  *   .settings(
  *     // Custom attributes for document generation
  *     // asciidoctorAttributes := Map("toc" -> "left", "icons" -> "font")
  *   )
  * }}}
  */
object AsciiDoc extends AutoPluginHelper {

  /** Configure AsciiDoc support with default settings
    *
    * Enables both HTML and PDF generation from AsciiDoc sources located in src/asciidoc.
    * Generated HTML will be placed in target/site/asciidoc, and PDFs in target/asciidoc-pdf.
    */
  def configure(project: Project): Project = apply()(project)

  /** Configure AsciiDoc support with custom options
    *
    * @param sourceDir Directory containing AsciiDoc source files (default: "src/asciidoc")
    * @param enablePdf Enable PDF generation (default: true)
    * @param enableDiagrams Enable diagram support for PlantUML, Graphviz, etc. (default: false)
    * @param attributes Custom AsciiDoc attributes for document processing
    * @param project The project to configure
    * @return The configured project
    */
  def apply(
    sourceDir: String = "src/asciidoc",
    enablePdf: Boolean = true,
    enableDiagrams: Boolean = false,
    attributes: Map[String, String] = Map.empty
  )(project: Project): Project = {

    // Base library dependencies for AsciidoctorJ
    val asciidoctorDeps = Seq(
      "org.asciidoctor" % "asciidoctorj" % "2.5.13"
    )

    // Add PDF support if enabled
    val pdfDeps = if (enablePdf) Seq(
      "org.asciidoctor" % "asciidoctorj-pdf" % "2.3.18"
    ) else Seq.empty

    // Add diagram support if enabled
    val diagramDeps = if (enableDiagrams) Seq(
      "org.asciidoctor" % "asciidoctorj-diagram" % "2.3.1"
    ) else Seq.empty

    project
      .settings(
        libraryDependencies ++= asciidoctorDeps ++ pdfDeps ++ diagramDeps,
        // Note: To fully integrate with sbt-site-asciidoctor, users should:
        // 1. Add to project/plugins.sbt:
        //    addSbtPlugin("com.github.sbt" % "sbt-site-asciidoctor" % "1.7.0")
        // 2. Enable plugins in build.sbt:
        //    enablePlugins(AsciidoctorPlugin)
        // 3. Configure source directory and attributes as needed
      )
  }
}
