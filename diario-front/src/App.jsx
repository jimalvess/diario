import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ConfigProvider, theme } from "antd";

import Login from "./pages/Login";
import Home from "./pages/Home";
import ResetPassword from "./pages/ResetPassword";
import Entradas from "./pages/Entradas";
import NovaEntrada from "./pages/NovaEntrada";
import EditarEntrada from "./pages/EditarEntrada";
import VisualizarEntrada from "./pages/VisualizarEntrada";
import PrivateRoute from "./components/PrivateRoute";

// --- Definição do Tema Escuro Suave ---
const softDarkTheme = {
  algorithm: theme.darkAlgorithm, // Algoritmo de tema escuro do Ant
  token: {
    colorPrimary: "#3498db",
    colorSuccess: "#2ecc71",
    colorWarning: "#f1c40f",
    colorError: "#e74c3c",
    colorInfo: "#3498db",
    colorTextBase: "#ecf0f1",
    colorBgBase: "#2c3e50",
    borderRadius: 8,
    boxShadowBase: "0 4px 10px rgba(0, 0, 0, 0.3)",
    fontFamily: "Inter, sans-serif",
  },
  components: {
    Layout: {
      headerBg: "rgba(44, 62, 80, 0.9)",
      footerBg: "transparent",
    },
    Menu: {
      darkItemBg: "transparent",
      darkItemSelectedBg: "rgba(52, 152, 219, 0.2)", // Fundo para item selecionado
      darkItemSelectedColor: "#ecf0f1", // Cor do texto do item selecionado
      darkSubMenuItemBg: "transparent",
      itemHoverColor: "#3498db", // Cor do texto ao passar o mouse
      itemActiveBg: "rgba(52, 152, 219, 0.2)", // Fundo do item ativo (clicado)
    },
    Table: {
      headerBg: "rgba(0, 0, 0, 0.3)", // Cabeçalho da tabela mais escuro
      rowHoverBg: "rgba(255, 255, 255, 0.08)",
      colorBgContainer: "rgba(0, 0, 0, 0.4)", // Fundo da tabela mais escuro que o Content
      colorText: "#ecf0f1", // Cor do texto dentro da tabela
      colorTextSecondary: "#bdc3c7",
      colorBorderSecondary: "rgba(255, 255, 255, 0.1)", // Bordas sutis
    },
    Card: {
      colorBgContainer: "rgba(0, 0, 0, 0.5)", // Fundo do Card
    },
    Input: {
      activeBorderColor: "#3498db",
      hoverBorderColor: "#3498db",
      activeShadow: "0 0 0 2px rgba(52, 152, 219, 0.2)",
      colorText: "#ecf0f1", // Cor do texto do input
      colorTextPlaceholder: "#bdc3c7",
      colorBgContainer: "rgba(255, 255, 255, 0.1)", // Fundo do input levemente transparente
    },
    Select: {
      optionSelectedBg: "rgba(52, 152, 219, 0.2)",
      optionActiveBg: "rgba(255, 255, 255, 0.1)",
      colorText: "#ecf0f1", // Cor do texto do select
      colorTextPlaceholder: "#bdc3c7",
      colorBgContainer: "rgba(255, 255, 255, 0.1)",
    },
    Button: {
      colorBgContainerDisabled: "rgba(255,255,255,0.05)",
      colorTextDisabled: "rgba(255,255,255,0.2)",
    },
  },
};

const App = () => {
  return (
    // ConfigProvider envolve toda a aplicação pra aplicar o tema softDarkTheme
    <ConfigProvider theme={softDarkTheme}>
      <Router>
        <Routes>
          {/* Rotas Públicas */}
          <Route path="/" element={<Login />} />
          <Route path="/login" element={<Login />} />
          <Route path="/redefinir-senha" element={<ResetPassword />} />

          {/* Rotas Protegidas (Exigem autenticação) */}
          <Route
            path="/home"
            element={
              <PrivateRoute>
                <Home />
              </PrivateRoute>
            }
          />
          <Route
            path="/entradas"
            element={
              <PrivateRoute>
                <Entradas />
              </PrivateRoute>
            }
          />
          <Route
            path="/entradas/nova"
            element={
              <PrivateRoute>
                <NovaEntrada />
              </PrivateRoute>
            }
          />
          <Route
            path="/entradas/:id"
            element={
              <PrivateRoute>
                <VisualizarEntrada />
              </PrivateRoute>
            }
          />
          <Route
            path="/entradas/editar/:id"
            element={
              <PrivateRoute>
                <EditarEntrada />
              </PrivateRoute>
            }
          />
        </Routes>
      </Router>
    </ConfigProvider>
  );
};

export default App;
