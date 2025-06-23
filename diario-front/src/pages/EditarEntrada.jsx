import React, { useState, useEffect } from "react";
import {
  Layout,
  Form,
  Input,
  Button,
  Upload,
  message,
  Card,
  Image,
  Row,
  Col,
  Modal, // Pra prévia de imagem
  Tooltip, // Pro o botão de remover mídia
  Typography, // Adicionado para Title
} from "antd";
import {
  SaveOutlined,
  CloseOutlined,
  PlusOutlined,
  DeleteOutlined, // Pro botão de remover mídia
  EyeOutlined,
  DownloadOutlined, // Pra visualização de documentos
} from "@ant-design/icons";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";

import backgroundImage from "../assets/background.jpg";
import HeaderDiario from "../components/HeaderDiario";

const { TextArea } = Input;
const { Content, Footer } = Layout;
const { Title } = Typography;

// Função auxiliar pra pré-visualização de imagem (pros novos uploads)
const getBase64 = (file) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });

const EditarEntrada = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true); // Estado de carregamento da página
  const [submitLoading, setSubmitLoading] = useState(false); // Estado de carregamento do formulário ao submeter

  // Estados pras mídias
  const [existingMedia, setExistingMedia] = useState([]); // Mídias já salvas no backend
  const [newFiles, setNewFiles] = useState([]); // Novos arquivos pra serem adicionados
  const [mediaToRemoveIds, setMediaToRemoveIds] = useState(new Set()); // IDs das mídias pra serem removidas

  // Estados proo modal de pré-visualização (novas e existentes)
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewContentUrl, setPreviewContentUrl] = useState("");
  const [previewModalTitle, setPreviewModalTitle] = useState("");

  const navigate = useNavigate();
  const { id } = useParams();
  const token = localStorage.getItem("token");
  const API_BASE_URL = "http://localhost:8080";

  // Handler pra cancelar a prévia do modal
  const handlePreviewModalCancel = () => {
    setPreviewOpen(false);
    setPreviewContentUrl("");
    setPreviewModalTitle("");
  };

  // Handler pra pré-visualizar NOVOS arquivos (Upload do Ant)
  const handleNewFilePreview = async (file) => {
    if (!file.url && !file.preview && file.originFileObj) {
      file.preview = await getBase64(file.originFileObj);
    }
    setPreviewContentUrl(file.url || file.preview);
    setPreviewOpen(true);
    setPreviewModalTitle(
      file.name ||
        (file.url
          ? file.url.substring(file.url.lastIndexOf("/") + 1)
          : "Visualização")
    );
  };

  // Handler pra pré-visualizar MÍDIAS EXISTENTES
  const handleExistingMediaPreview = (url, name, type) => {
    setPreviewContentUrl(url);
    setPreviewModalTitle(name);
    setPreviewOpen(true);
  };

  // Handler pra mudanças nos novos arquivos (Upload do Ant)
  const handleChangeNewFiles = ({ fileList: newFileList }) => {
    const filteredList = newFileList.filter((file) => {
      const isLt20M = file.size / 1024 / 1024 < 20; // Limite de 20MB por arquivo
      if (!isLt20M) {
        message.error(`${file.name} excede o tamanho máximo de 20MB!`);
      }
      return isLt20M;
    });
    setNewFiles(filteredList);
  };

  // Handler pra remover uma mídia existente
  const handleRemoveExistingMedia = (mediaId) => {
    setExistingMedia((prev) =>
      prev.filter((media) => media.uid !== mediaId.toString())
    ); // Compara uid string
    setMediaToRemoveIds((prev) => new Set(prev).add(mediaId)); // Adiciona o ID numérico ao Set
    message.info("Mídia marcada para remoção. Salve para confirmar.");
  };

  // --- Efeito para carregar dados da entrada e mídias existentes ---
  useEffect(() => {
    const fetchEntrada = async () => {
      setLoading(true);
      try {
        if (!token) {
          throw new Error("Authentication token not found.");
        }

        const entradaResponse = await axios.get(
          `${API_BASE_URL}/api/entradas/${id}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        const entrada = entradaResponse.data;
        form.setFieldsValue({
          titulo: entrada.titulo,
          conteudo: entrada.conteudo,
        });

        // Mapear mídias existentes pro formato do fileList do Ant pra exibir
        const loadedMedia = entrada.midias
          ? entrada.midias.map((media) => {
              const mediaFileName = media.caminhoArquivo
                ? media.caminhoArquivo.split(/[\\/]/).pop()
                : media.nomeOriginalArquivo; // Fallback se caminhoArquivo for undefined/null

              const mediaUrl = mediaFileName
                ? `${API_BASE_URL}/api/entradas/arquivo/${mediaFileName}`
                : null;

              return {
                uid: media.id.toString(), // UID tem que ser string
                name: media.nomeOriginalArquivo || "arquivo-desconhecido", // Fallback pro nome
                status: "done",
                url: mediaUrl,
                response: media, // Guarda o objeto original da mídia
                isExisting: true, // Flag para identificar que é uma mídia existente
                type: media.tipoArquivo, // Pra ajudar na renderização
              };
            })
          : [];
        setExistingMedia(loadedMedia);
      } catch (error) {
        console.error("Erro ao carregar entrada ou mídias:", error);
        message.error("Erro ao carregar a entrada para edição.");
        navigate("/entradas");
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchEntrada();
    } else {
      message.error("ID da entrada não fornecido.");
      navigate("/entradas");
    }
  }, [id, token, navigate, form, API_BASE_URL]);

  // --- Handler pra submissão do formulário (Atualizar) ---
  const onFinish = async (values) => {
    setSubmitLoading(true); // Ativa loading pro botão de submissão
    try {
      const formData = new FormData();
      formData.append("titulo", values.titulo);
      formData.append("conteudo", values.conteudo);

      // Adiciona novos arquivos
      newFiles.forEach((file) => {
        formData.append("novosArquivos", file.originFileObj);
      });

      // Adiciona IDs das mídias a serem removidas
      if (mediaToRemoveIds.size > 0) {
        mediaToRemoveIds.forEach((idToRemove) => {
          formData.append("idsMidiasRemover", idToRemove);
        });
      }

      const response = await axios.put(
        `${API_BASE_URL}/api/entradas/${id}`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );

      if (response.status === 200) {
        message.success("Entrada atualizada com sucesso!");
        navigate("/entradas");
      } else {
        console.warn("Status inesperado:", response);
        message.error("Erro ao atualizar entrada.");
      }
    } catch (error) {
      console.error("Erro ao atualizar:", error);
      if (error.response?.status === 413) {
        message.error("Um ou mais arquivos são muito grandes!");
      } else if (error.response?.status === 403) {
        message.error("Você não tem permissão para atualizar esta entrada.");
      } else {
        message.error(
          "Erro ao atualizar entrada. Verifique o console para mais detalhes."
        );
      }
    } finally {
      setSubmitLoading(false); // Desativa loading
    }
  };

  // Props pro componente Upload de novos arquivos
  const newUploadProps = {
    listType: "picture-card",
    fileList: newFiles,
    onPreview: handleNewFilePreview, // Usa o novo handler pra novos arquivos
    onChange: handleChangeNewFiles,
    beforeUpload: () => false, // Evita upload automático
    multiple: true,
    accept:
      "image/*,video/*,audio/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  };

  const uploadButton = (
    <button
      style={{
        border: 0,
        background: "none",
      }}
      type="button"
    >
      <PlusOutlined />
      <div style={{ marginTop: 8 }}>Upload</div>
    </button>
  );

  const handleCancelar = () => {
    navigate("/entradas");
  };

  // Renderiza o estado de carregamento inicial
  if (loading) {
    return (
      <Layout
        style={{
          minHeight: "100vh",
          width: "100vw",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          backgroundColor: "#2c3e50",
        }}
      >
        {" "}
        {/* Fundo escuro simples */}
        <p
          style={{
            color: "white",
            fontSize: "1.2rem",
            padding: "20px",
            backgroundColor: "rgba(0,0,0,0.7)",
            borderRadius: "8px",
          }}
        >
          Carregando entrada...
        </p>
      </Layout>
    );
  }

  return (
    <Layout style={{ minHeight: "100vh", width: "100vw" }}>
      <HeaderDiario />

      <Content
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          flexDirection: "column",
          padding: "3rem 2rem",
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundColor: "#2c3e50", // Fundo escuro simples
          minHeight: "calc(100vh - 64px - 80px)",
          color: "white",
          paddingTop: "84px", // Distância do cabeçalho
        }}
      >
        <Card
          style={{
            width: "100%",
            maxWidth: "800px", // Aumentei a largura máxima para acomodar as mídias
            padding: "1.5rem",
            textAlign: "left",
            backgroundColor: "rgba(44, 62, 80, 0.9)", // Fundo do Card ligeiramente transparente
            boxShadow: "0px 4px 15px rgba(0, 0, 0, 0.5)", // Sombra do Card
            borderRadius: "8px",
          }}
        >
          <Title
            level={2}
            style={{
              color: "white",
              textAlign: "center",
              marginBottom: "1.5rem",
            }}
          >
            Edite sua Entrada
          </Title>

          <Form form={form} layout="vertical" onFinish={onFinish}>
            <Row gutter={[16, 16]}>
              <Col span={24}>
                <Form.Item
                  label={<span style={{ color: "white" }}>Título</span>} // Cor do label
                  name="titulo"
                  rules={[
                    { required: true, message: "Digite o título da entrada" },
                  ]}
                >
                  <TextArea
                    rows={1}
                    placeholder="Digite o título"
                    style={{
                      backgroundColor: "#444",
                      color: "white",
                      borderColor: "#555",
                    }}
                  />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item
                  label={<span style={{ color: "white" }}>Conteúdo</span>} // Cor do label
                  name="conteudo"
                  rules={[
                    { required: true, message: "Digite o conteúdo da entrada" },
                  ]}
                >
                  <TextArea
                    rows={6}
                    placeholder="Como foi seu dia?"
                    style={{
                      backgroundColor: "#444",
                      color: "white",
                      borderColor: "#555",
                    }}
                  />
                </Form.Item>
              </Col>

              {/* Seção para mídias existentes */}
              {existingMedia.length > 0 && (
                <Col span={24}>
                  <Form.Item
                    label={
                      <span style={{ color: "white" }}>Mídias Existentes</span>
                    }
                  >
                    <div
                      style={{
                        display: "flex",
                        flexWrap: "wrap",
                        gap: "10px",
                        justifyContent: "center",
                      }}
                    >
                      {existingMedia.map((media) => {
                        const isImage = media.type === "imagem";
                        const isVideo = media.type === "video";
                        const isAudio = media.type === "audio";

                        return (
                          <Card
                            key={media.uid}
                            size="small"
                            style={{
                              width: 120,
                              textAlign: "center",
                              backgroundColor: "rgba(0,0,0,0.4)",
                              borderColor: "#555",
                              color: "white",
                            }}
                            bodyStyle={{ padding: "8px" }}
                            cover={
                              isImage ? (
                                <Image
                                  alt={media.name}
                                  src={media.url}
                                  style={{
                                    height: 80,
                                    objectFit: "contain",
                                    padding: "5px",
                                  }}
                                  preview={{ src: media.url }} // Habilita a prévia em modal para imagens
                                />
                              ) : isVideo ? (
                                <video
                                  controls
                                  style={{
                                    height: 80,
                                    width: "100%",
                                    objectFit: "contain",
                                  }}
                                >
                                  <source
                                    src={media.url}
                                    type={`${media.type}/${media.name
                                      .split(".")
                                      .pop()}`}
                                  />
                                  Seu navegador não suporta a tag de vídeo.
                                </video>
                              ) : isAudio ? (
                                <audio
                                  controls
                                  style={{ width: "100%", marginTop: "5px" }}
                                >
                                  <source
                                    src={media.url}
                                    type={`${media.type}/${media.name
                                      .split(".")
                                      .pop()}`}
                                  />
                                  Seu navegador não suporta a tag de áudio.
                                </audio>
                              ) : (
                                // Para documentos: Torna clicável
                                <div
                                  style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    height: 80,
                                    color: "white",
                                  }}
                                >
                                  <Button
                                    type="link"
                                    icon={<EyeOutlined />}
                                    onClick={() =>
                                      handleExistingMediaPreview(
                                        media.url,
                                        media.name,
                                        media.type
                                      )
                                    }
                                    style={{
                                      color: "#1890ff",
                                      fontSize: "0.8rem",
                                    }}
                                  >
                                    Visualizar
                                  </Button>
                                  <a
                                    href={media.url}
                                    download={media.name}
                                    style={{
                                      color: "#52c41a",
                                      fontSize: "0.75rem",
                                      marginTop: "5px",
                                    }}
                                  >
                                    <DownloadOutlined
                                      style={{ marginRight: 4 }}
                                    />
                                    Baixar
                                  </a>
                                </div>
                              )
                            }
                            actions={[
                              <Tooltip title="Remover Mídia">
                                <Button
                                  type="text"
                                  icon={
                                    <DeleteOutlined style={{ color: "red" }} />
                                  }
                                  onClick={() =>
                                    handleRemoveExistingMedia(
                                      parseInt(media.uid)
                                    )
                                  } // Converte UID de volta para número
                                  danger
                                />
                              </Tooltip>,
                            ]}
                          >
                            <Card.Meta
                              title={
                                <span style={{ color: "white" }}>
                                  {media.name}
                                </span>
                              }
                            />
                          </Card>
                        );
                      })}
                    </div>
                  </Form.Item>
                </Col>
              )}

              {/* Seção para adicionar novas mídias */}
              <Col span={24}>
                <Form.Item
                  label={
                    <span style={{ color: "white" }}>
                      Adicionar Novas Mídias (opcional)
                    </span>
                  }
                >
                  <Upload {...newUploadProps}>
                    {newFiles.length + existingMedia.length >= 8
                      ? null
                      : uploadButton}
                  </Upload>
                </Form.Item>
              </Col>
            </Row>

            <Form.Item style={{ textAlign: "right", marginTop: "1.5rem" }}>
              <Button
                type="primary"
                htmlType="submit"
                loading={submitLoading}
                icon={<SaveOutlined />}
                style={{ marginRight: "10px" }}
              >
                Atualizar
              </Button>
              <Button
                type="default"
                onClick={handleCancelar}
                icon={<CloseOutlined />}
              >
                Cancelar
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Content>

      {/* Modal para prévia de documentos e novas imagens */}
      <Modal
        open={previewOpen}
        title={previewModalTitle}
        footer={null}
        onCancel={handlePreviewModalCancel}
        width="80%"
      >
        {previewContentUrl &&
          (previewContentUrl.includes(".pdf") ? (
            <iframe
              src={previewContentUrl}
              style={{ width: "100%", height: "70vh", border: "none" }}
            />
          ) : (
            // Pra outros tipos de documentos, ou visualização de imagem normal
            <img
              alt="prévia"
              style={{
                maxWidth: "100%",
                maxHeight: "70vh",
                objectFit: "contain",
              }}
              src={previewContentUrl}
            />
          ))}
      </Modal>

      <Footer
        style={{
          textAlign: "center",
          color: "#fff",
          backgroundColor: "#2c3e50",
          fontSize: "0.8rem",
        }}
      >
        © {new Date().getFullYear()} Powered by Jim Alves
      </Footer>
    </Layout>
  );
};

export default EditarEntrada;
