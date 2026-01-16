import java.nio.file.{Files, Path}

enablePlugins(OssumIncPlugin)

lazy val root = Root("asciidoc-test", startYr = 2024)
  .configure(With.basic, With.AsciiDoc(enableDiagrams = false))
  .settings(
    maxErrors := 50,
    // Custom task to check that AsciiDoc dependencies are present
    TaskKey[Unit]("checkDependencies") := {
      val deps = libraryDependencies.value
      val hasAsciidoctorj = deps.exists(m =>
        m.organization == "org.asciidoctor" && m.name == "asciidoctorj"
      )
      val hasAsciidoctorjPdf = deps.exists(m =>
        m.organization == "org.asciidoctor" && m.name == "asciidoctorj-pdf"
      )
      require(hasAsciidoctorj, "asciidoctorj dependency not found")
      require(hasAsciidoctorjPdf, "asciidoctorj-pdf dependency not found")
      println("AsciiDoc dependencies verified successfully")
    },
    // Custom task to generate PDF from AsciiDoc using AsciidoctorJ
    TaskKey[Unit]("generatePdf") := {
      val cp = (Compile / dependencyClasspath).value
      val log = streams.value.log
      val srcDir = baseDirectory.value / "src" / "asciidoc"
      val outDir = target.value / "asciidoc-pdf"

      if (!outDir.exists()) Files.createDirectories(outDir.toPath)

      val adocFiles = (srcDir ** "*.adoc").get
      require(adocFiles.nonEmpty, s"No .adoc files found in $srcDir")

      val asciidoctor = Class.forName("org.asciidoctor.Asciidoctor$Factory", true,
        new java.net.URLClassLoader(cp.map(_.data.toURI.toURL).toArray, null))
        .getMethod("create")
        .invoke(null)

      val optionsBuilderClass = Class.forName("org.asciidoctor.Options", true, asciidoctor.getClass.getClassLoader)
      val builderMethod = optionsBuilderClass.getMethod("builder")
      val builder = builderMethod.invoke(null)

      val safeClass = Class.forName("org.asciidoctor.SafeMode", true, asciidoctor.getClass.getClassLoader)
      val unsafeMode = safeClass.getField("UNSAFE").get(null)

      val builderClass = builder.getClass
      builderClass.getMethod("safe", safeClass).invoke(builder, unsafeMode)
      builderClass.getMethod("backend", classOf[String]).invoke(builder, "pdf")
      builderClass.getMethod("toDir", classOf[java.io.File]).invoke(builder, outDir)

      val options = builderClass.getMethod("build").invoke(builder)

      adocFiles.foreach { adocFile =>
        log.info(s"Generating PDF from: ${adocFile.getName}")
        val convertMethod = asciidoctor.getClass.getMethod("convertFile", classOf[java.io.File], optionsBuilderClass)
        convertMethod.invoke(asciidoctor, adocFile, options)
      }

      log.success(s"PDF generation complete. Output in: $outDir")
    },
    // Custom task to generate HTML from AsciiDoc
    TaskKey[Unit]("generateSite") := {
      val cp = (Compile / dependencyClasspath).value
      val log = streams.value.log
      val srcDir = baseDirectory.value / "src" / "asciidoc"
      val outDir = target.value / "asciidoc-site"

      if (!outDir.exists()) Files.createDirectories(outDir.toPath)

      val adocFiles = (srcDir ** "*.adoc").get
      require(adocFiles.nonEmpty, s"No .adoc files found in $srcDir")

      val asciidoctor = Class.forName("org.asciidoctor.Asciidoctor$Factory", true,
        new java.net.URLClassLoader(cp.map(_.data.toURI.toURL).toArray, null))
        .getMethod("create")
        .invoke(null)

      val optionsBuilderClass = Class.forName("org.asciidoctor.Options", true, asciidoctor.getClass.getClassLoader)
      val builderMethod = optionsBuilderClass.getMethod("builder")
      val builder = builderMethod.invoke(null)

      val safeClass = Class.forName("org.asciidoctor.SafeMode", true, asciidoctor.getClass.getClassLoader)
      val unsafeMode = safeClass.getField("UNSAFE").get(null)

      val builderClass = builder.getClass
      builderClass.getMethod("safe", safeClass).invoke(builder, unsafeMode)
      builderClass.getMethod("backend", classOf[String]).invoke(builder, "html5")
      builderClass.getMethod("toDir", classOf[java.io.File]).invoke(builder, outDir)

      val options = builderClass.getMethod("build").invoke(builder)

      adocFiles.foreach { adocFile =>
        log.info(s"Generating HTML from: ${adocFile.getName}")
        val convertMethod = asciidoctor.getClass.getMethod("convertFile", classOf[java.io.File], optionsBuilderClass)
        convertMethod.invoke(asciidoctor, adocFile, options)
      }

      log.success(s"Site generation complete. Output in: $outDir")
    },
    // Task to verify PDF output exists
    TaskKey[Unit]("checkPdfOutput") := {
      val outDir = target.value / "asciidoc-pdf"
      val pdfFiles = (outDir ** "*.pdf").get
      require(pdfFiles.nonEmpty, s"No PDF files found in $outDir")
      pdfFiles.foreach { f =>
        println(s"Found PDF: ${f.getName} (${f.length()} bytes)")
        require(f.length() > 0, s"PDF file ${f.getName} is empty")
      }
    },
    // Task to verify HTML output exists
    TaskKey[Unit]("checkSiteOutput") := {
      val outDir = target.value / "asciidoc-site"
      val htmlFiles = (outDir ** "*.html").get
      require(htmlFiles.nonEmpty, s"No HTML files found in $outDir")
      htmlFiles.foreach { f =>
        println(s"Found HTML: ${f.getName} (${f.length()} bytes)")
        require(f.length() > 0, s"HTML file ${f.getName} is empty")
      }
    }
  )
