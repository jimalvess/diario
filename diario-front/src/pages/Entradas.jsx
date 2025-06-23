import React, { useState, useEffect } from "react";
import { Layout, Button, Table, Tooltip, Popconfirm, message } from "antd";
import {
  FileAddOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import backgroundImage from "../assets/background.jpg";
import HeaderDiario from "../components/HeaderDiario";

const { Content, Footer } = Layout;

const Entradas = () => {
  const [entradas, setEntradas] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEntradas = async () => {
      setLoading(true);
      try {
        const token = localStorage.getItem("token");
        const usuarioId = localStorage.getItem("usuarioId");
        if (!usuarioId || !token) {
          throw new Error("Usuário ou token não encontrados no localStorage");
        }

        const url = `/api/entradas`;

        const response = await fetch(url, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          const text = await response.text();
          console.error("Resposta não OK:", response.status, text);
          throw new Error(`Erro ao buscar entradas: ${response.status}`);
        }

        const text = await response.text();

        let data;
        try {
          data = JSON.parse(text);
        } catch (jsonError) {
          console.error("Erro ao converter resposta para JSON:", text);
          throw new Error("Resposta da API não é JSON válido");
        }

        setEntradas(Array.isArray(data) ? data : data.entradas || []);
      } catch (error) {
        console.error("Erro ao carregar as entradas:", error);
        message.error("Erro ao carregar as entradas. Veja console.");
      } finally {
        setLoading(false);
      }
    };

    fetchEntradas();
  }, []);

  // Handlers pra ações da tabela
  const handleVer = (id) => {
    navigate(`/entradas/${id}`);
  };

  const handleEditar = (id) => {
    navigate(`/entradas/editar/${id}`);
  };

  const handleExcluir = async (id) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`http://localhost:8080/api/entradas/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) {
        throw new Error("Erro ao excluir");
      }
      message.success("Entrada excluída com sucesso.");
      setEntradas((prev) => prev.filter((e) => e.id !== id));
    } catch (error) {
      console.error("Erro ao excluir a entrada:", error);
      message.error("Erro ao excluir a entrada.");
    }
  };

  // Colunas da tabela
  const columns = [
    {
      title: "DATA",
      dataIndex: "data",
      key: "data",
      sorter: (a, b) => new Date(a.data) - new Date(b.data),
      render: (text) => new Date(text).toLocaleDateString("pt-BR"),
    },
    {
      title: "TÍTULO",
      dataIndex: "titulo",
      key: "titulo",
      ellipsis: true, // Adiciona reticências se o texto for muito longo
    },
    {
      title: "AÇÕES",
      key: "acoes",
      render: (_, record) => (
        <>
          <Tooltip title="Ver">
            <Button
              type="default"
              icon={<EyeOutlined />}
              style={{
                backgroundColor: "green",
                color: "#FFFFFF",
                marginRight: 15,
              }}
              onClick={() => handleVer(record.id)}
            />
          </Tooltip>
          <Tooltip title="Editar">
            <Button
              type="default"
              icon={<EditOutlined />}
              style={{
                backgroundColor: "orange",
                color: "#FFFFFF",
                marginRight: 15,
              }}
              onClick={() => handleEditar(record.id)}
            />
          </Tooltip>
          <Tooltip title="Excluir">
            <Popconfirm
              title="Tem certeza que deseja excluir?"
              onConfirm={() => handleExcluir(record.id)}
              okText="Sim"
              cancelText="Não"
            >
              <Button
                type="link"
                icon={<DeleteOutlined />}
                style={{
                  backgroundColor: "red",
                  color: "#FFFFFF",
                }}
                danger
              />
            </Popconfirm>
          </Tooltip>
        </>
      ),
    },
  ];

  return (
    <Layout style={{ minHeight: "100vh", width: "100vw" }}>
      <HeaderDiario />

      <Content
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          padding: "3rem 2rem",
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          minHeight: "calc(100vh - 64px - 80px)",
          color: "white",
          paddingTop: "84px", // Distância do cabeçalho
        }}
      >
        {/* Container pro botão e a tabela, pra limitar a largura e aplicar sombra */}
        <div
          style={{
            width: "100%",
            maxWidth: "800px",
            marginBottom: "20px",
            textAlign: "right",
          }}
        >
          <Button
            type="primary"
            onClick={() => navigate("/entradas/nova")}
            icon={<FileAddOutlined />}
          >
            Nova Entrada
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={entradas}
          rowKey="id"
          loading={loading}
          locale={{
            emptyText: (
              <span style={{ color: "inherit" }}>
                Nenhuma entrada encontrada.
              </span>
            ),
          }} // 'inherit' herda a cor do tema
          pagination={{ pageSize: 9 }}
          style={{
            width: "100%",
            maxWidth: "800px", // A tabela ocupa a largura máxima do container
            borderRadius: 8, // Arredondamento das bordas
            overflow: "hidden", // Garante que o borderRadius funcione com o fundo da tabela
            backgroundColor: "#2c3e50", // Fundo escuro simples
            boxShadow: "0px 4px 15px rgba(0, 0, 0, 0.5)", // Sombra da tabela
          }}
        />
      </Content>

      <Footer
        style={{
          textAlign: "center",
          color: "#fff",
          backgroundColor: "transparent",
          fontSize: "0.8rem",
        }}
      >
        © {new Date().getFullYear()} Powered by Jim Alves
      </Footer>
    </Layout>
  );
};

export default Entradas;
