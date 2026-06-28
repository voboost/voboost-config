# Voboost Config - Code Style (CRITICAL)

## Global
- This project follows ALL common rules from ../voboost-codestyle/AGENTS.md

## Project Structure
- Library code in src/main/java/ru/voboost/config/
- Demo app available as separate voboost-config-demo project

## Critical Rules
- Config structure is completely FLAT - no nested objects
- ALL ConfigManager methods MUST use recursive reflection
- NEVER use @ConfigAlias annotations
- ALL fields in Config are required when loaded from filesystem
- Use Mock Context for Android-specific operations in tests
