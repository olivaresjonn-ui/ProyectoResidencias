import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: {
    extend: {
      colors: {
        ink: "#172b4d",
        brand: "#3157d5",
        canvas: "#f5f7fb",
      },
      boxShadow: {
        card: "0 10px 35px rgba(25, 43, 78, 0.06)",
      },
    },
  },
  plugins: [],
};

export default config;
