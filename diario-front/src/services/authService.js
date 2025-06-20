import api from './api';

//função de login
export const login = async (username, senha) => {
  const response = await api.post('/auth/login', { username, senha });
  return response.data; // Esperado: { token: "..." }
};
