CREATE TABLE "user"
(
  "id"         UUID      NOT NULL,
  "email"      TEXT      NOT NULL,
  "first_name" TEXT      NOT NULL,
  "last_name"  TEXT      NOT NULL,
  "password"   TEXT      NOT NULL,
  "created_at" TIMESTAMP NOT NULL,
  "updated_at" TIMESTAMP
);

ALTER TABLE "user"
  ADD CONSTRAINT "pk_user" PRIMARY KEY ("id");

--------------------------------------------------
--------------------------------------------------

CREATE TABLE "session"
(
  "id"         UUID      NOT NULL,
  "user_id"    UUID      NOT NULL,
  "created_at" TIMESTAMP NOT NULL,
  "updated_at" TIMESTAMP
);

ALTER TABLE "session"
  ADD CONSTRAINT "pk_session" PRIMARY KEY ("id"),
  ADD CONSTRAINT "fk_session_user" FOREIGN KEY ("user_id") REFERENCES "user" ("id");
