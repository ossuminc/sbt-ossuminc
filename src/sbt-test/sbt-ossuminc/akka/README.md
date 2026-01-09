# Akka Test

This test verifies that the Akka helper correctly configures Akka dependencies and resolvers.

## Requirements

**Akka Repository Token Required**: As of October 2024, Akka requires a repository access token.

### Getting a Token

1. Visit https://akka.io/key
2. Register for a free token
3. Set the environment variable:
   ```bash
   export AKKA_REPO_TOKEN=your-token-here
   ```

### Running the Test

```bash
# With environment variable set:
AKKA_REPO_TOKEN=your-token sbt "scripted sbt-ossuminc/akka"

# Or add to your shell profile:
echo 'export AKKA_REPO_TOKEN=your-token' >> ~/.zshrc
source ~/.zshrc
sbt "scripted sbt-ossuminc/akka"
```

## What This Test Does

1. Configures a project with `With.Akka.configure`
2. Runs `update` to resolve Akka dependencies
3. Runs the sample Akka application to verify everything works

## License Information

- **Akka License**: Business Source License 1.1 (Lightbend, Inc.)
- **Free for development**: Production use may require a commercial license
- **More info**: https://akka.io/blog/news/2024/10/02/akka-2.9.6-released
