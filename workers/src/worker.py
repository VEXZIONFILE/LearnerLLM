from workers import WorkerEntrypoint

from learner_api.main import app


class Default(WorkerEntrypoint):
    async def fetch(self, request):
        import asgi

        return await asgi.fetch(app, request.js_object, self.env)
