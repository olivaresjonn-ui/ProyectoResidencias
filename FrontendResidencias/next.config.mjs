/** @type {import('next').NextConfig} */
const nextConfig = {
  // Limita los workers para que la compilación funcione en equipos con poca RAM.
  experimental: {
    cpus: 1,
    workerThreads: false,
  },
};

export default nextConfig;
