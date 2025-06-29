import axios from "axios";

const api = axios.create({
  // Usa a variável de ambiente VITE_APP_API_URL. Ele expõe variáveis de ambiente que começam com VITE_ como import.meta.env.VITE_NOME_DA_VARIAVEL
  // No deploy, usa o back deployado, ou, usa o local.
  baseURL: import.meta.env.VITE_APP_API_URL || "http://localhost:8080",
});

export default api;
