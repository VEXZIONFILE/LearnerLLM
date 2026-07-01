import json
from datetime import datetime
from typing import Any


def _parse_dt(value: Any) -> datetime | None:
    if value is None:
        return None
    if isinstance(value, datetime):
        return value
    text = str(value)
    if text.endswith("Z"):
        text = text[:-1] + "+00:00"
    try:
        return datetime.fromisoformat(text)
    except ValueError:
        return None


class D1Database:
    def __init__(self, binding) -> None:
        self._db = binding

    async def execute(self, sql: str, *params: Any) -> None:
        stmt = self._db.prepare(sql)
        if params:
            stmt = stmt.bind(*params)
        await stmt.run()

    async def fetchone(self, sql: str, *params: Any) -> dict[str, Any] | None:
        stmt = self._db.prepare(sql)
        if params:
            stmt = stmt.bind(*params)
        row = await stmt.first()
        if row is None:
            return None
        return dict(row)

    async def fetchall(self, sql: str, *params: Any) -> list[dict[str, Any]]:
        stmt = self._db.prepare(sql)
        if params:
            stmt = stmt.bind(*params)
        result = await stmt.run()
        rows = getattr(result, "results", result)
        if rows is None:
            return []
        return [dict(row) for row in rows]

    async def insert_returning_id(self, sql: str, *params: Any) -> int:
        await self.execute(sql, *params)
        row = await self.fetchone("SELECT last_insert_rowid() AS id")
        return int(row["id"]) if row else 0


async def get_db(request) -> D1Database:
    env = request.scope["env"]
    return D1Database(env.DB)
