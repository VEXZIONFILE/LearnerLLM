export interface Env {
  VPC_SERVICE: Fetcher;
  /** Must match the host:port registered on your VPC Service (default backend uvicorn). */
  VPC_TARGET_ORIGIN: string;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const incoming = new URL(request.url);
    const origin = env.VPC_TARGET_ORIGIN || "http://localhost:8080";
    const target = new URL(`${incoming.pathname}${incoming.search}`, origin);

    const hasBody = request.method !== "GET" && request.method !== "HEAD";

    const proxyRequest = new Request(target, {
      method: request.method,
      headers: request.headers,
      body: hasBody ? request.body : undefined,
      redirect: "manual",
    });

    return env.VPC_SERVICE.fetch(proxyRequest);
  },
} satisfies ExportedHandler<Env>;
