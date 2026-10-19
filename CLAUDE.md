# CLAUDE.md

Guía para Claude Code al trabajar en este proyecto.

## Estructura

- `demo/` — proyecto Java (Maven, con `mvnw`).
- `.claude/skills/` — Agent Skills del proyecto.
  - `frontend-design/` — guía de diseño visual para construir o rediseñar interfaces (fuente: github.com/anthropics/skills).
- `.claude/agents/` — subagentes personalizados del proyecto (archivos `.md`).

## Comandos

```bash
cd demo && ./mvnw spring-boot:run   # ejecutar
cd demo && ./mvnw test              # pruebas
```

## Flujo de trabajo con git

- `main` es la rama estable y la que se sube a GitHub. Nunca se trabaja directamente sobre ella.
- Cada solicitud de cambio abre una rama nueva desde `main` actualizado: `feature/<descripcion-corta>` o `fix/<descripcion-corta>`.
- Se hacen commits pequeños y descriptivos en esa rama mientras se trabaja.
- Solo cuando el usuario aprueba el cambio: merge a `main` (`git merge --no-ff`) y `git push origin main`. Después se borra la rama.
- Sin aprobación explícita no se hace merge ni push a `main`.

## Convenciones

- Responder en español.
- Al construir o modificar interfaces (HTML/CSS/plantillas), usar la skill `frontend-design`.
