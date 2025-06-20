import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080', // back rodando
});

export default api;
