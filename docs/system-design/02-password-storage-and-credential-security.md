# 02 - Password Storage and Credential Security

## Goal

Understand why passwords must never be stored in plain text and how to apply secure password handling in this project.

## What changed in this project

- New user passwords are hashed with BCrypt at registration.
- Login verifies using BCrypt hash comparison.
- Legacy plain-text passwords are migrated to BCrypt on successful login.
- Demo seed user is stored as BCrypt and auto-upgraded if legacy data exists.

Code:

- [PasswordConfig.java](../../src/main/java/com/instagram/backend/config/PasswordConfig.java)
- [AuthService.java](../../src/main/java/com/instagram/backend/service/AuthService.java)
- [DemoUserInitializer.java](../../src/main/java/com/instagram/backend/config/DemoUserInitializer.java)

## Why BCrypt

- Slow, adaptive hashing function designed for passwords.
- Includes per-password salt.
- Increases attacker cost if DB is leaked.

## Design choices

1. `PasswordEncoder` bean centralizes hashing policy.
2. Registration path always hashes.
3. Login path supports migration for old plain-text rows.
4. No user-facing API changes needed for migration.

## Risks and next hardening steps

- Add password policy (min length/complexity/deny common passwords).
- Add rate limiting + lockout for repeated failed logins.
- Add auditing for suspicious auth behavior.
- Add forced password reset flow for compromised accounts.

## Implementation result

Verified with live DB check:
- `users.password` is now stored as BCrypt hash prefix `$2a$`.
