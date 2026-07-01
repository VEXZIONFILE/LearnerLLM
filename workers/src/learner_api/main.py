from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from learner_api.routers import billing, chat, me, progress, reports, scans, subjects


def create_app() -> FastAPI:
    app = FastAPI(title="LearnerLM API")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
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
