from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from learner_api.config import get_settings
from learner_api.database import init_db
from learner_api.routers import billing, chat, me, progress, reports, scans, subjects


@asynccontextmanager
async def lifespan(_: FastAPI):
    await init_db()
    yield


def create_app() -> FastAPI:
    settings = get_settings()
    app = FastAPI(title=settings.app_name, lifespan=lifespan)

    origins = [o.strip() for o in settings.cors_origins.split(",") if o.strip()]
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins or ["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(me.router)
    app.include_router(chat.router)
    app.include_router(scans.router)
    app.include_router(billing.router)
    app.include_router(subjects.router)
    app.include_router(progress.router)
    app.include_router(reports.router)
    return app


app = create_app()
