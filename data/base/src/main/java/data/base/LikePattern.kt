package data.base

/** 검색어를 SQL LIKE 패턴(`%검색어%`)으로 바꾼다. 사용자가 입력한 % · _ · \ 가 와일드카드로 동작하지 않게 `\` 로 이스케이프한다. */
fun String.toLikePattern(): String =
    "%" + replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
