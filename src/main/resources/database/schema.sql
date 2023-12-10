create Table if not EXISTS  public.checks(
  id BIGSERIAL PRIMARY KEY,
  chat_id BIGSERIAL not null,
  check_date date not null,
  check_text varchar(400) not null
);

create Table if not EXISTS  public.notes(
  id BIGSERIAL PRIMARY KEY,
  chat_id BIGSERIAL not null,
  note_text varchar(400) not null
);


/*
1 Таблица чеков
- id - PK
- дата чека
- текст чека
- id чата

	2 Таблица заметок
- id - PK
- id чата
- текст заметки*/
