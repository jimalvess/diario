import React, { useState } from 'react';
import {
    Layout,
    Form,
    Input,
    Button,
    Upload,
    message,
    Card,
    Row,
    Col,
    Modal, // Pra prévia de imagem
} from 'antd';
import {
    UploadOutlined,
    SaveOutlined,
    CloseOutlined,
    PlusOutlined, 
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import backgroundImage from '../assets/background.jpg';
import HeaderDiario from '../components/HeaderDiario';

const { TextArea } = Input;
const { Content, Footer } = Layout;

// Função auxiliar para pré-visualização de imagem
const getBase64 = (file) =>
    new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = (error) => reject(error);
    });

const NovaEntrada = () => {
    // Agora, um array para armazenar os arquivos a serem enviados
    const [fileList, setFileList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [previewOpen, setPreviewOpen] = useState(false); // Estado para controlar o modal de prévia
    const [previewImage, setPreviewImage] = useState(''); // URL da imagem para prévia
    const [previewTitle, setPreviewTitle] = useState(''); // Título da prévia
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    // Handler para cancelar a prévia da imagem
    const handlePreviewCancel = () => setPreviewOpen(false);

    // Handler para pré-visualizar a imagem
    const handlePreview = async (file) => {
        if (!file.url && !file.preview) {
            file.preview = await getBase64(file.originFileObj);
        }
        setPreviewImage(file.url || file.preview);
        setPreviewOpen(true);
        setPreviewTitle(file.name || file.url.substring(file.url.lastIndexOf('/') + 1));
    };

    // Handler para mudanças na lista de arquivos do Upload
    const handleChange = ({ fileList: newFileList }) => {
        // Filtra arquivos que excedam o limite de 20MB
        const filteredList = newFileList.filter(file => {
            const isLt20M = file.size / 1024 / 1024 < 20; 
            if (!isLt20M) {
                message.error(`${file.name} excede o tamanho máximo de 20MB!`);
            }
            return isLt20M;
        });
        setFileList(filteredList);
    };

    const onFinish = async (values) => {
        setLoading(true);
        try {
            const formData = new FormData();
            formData.append('titulo', values.titulo);
            formData.append('conteudo', values.conteudo);

            // Adiciona todos os arquivos do fileList ao FormData sob a chave 'arquivos'
            fileList.forEach(file => {
                formData.append('arquivos', file.originFileObj);
            });

            const response = await axios.post('http://localhost:8080/api/entradas', formData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'multipart/form-data',
                },
            });

            if (response.status === 200) {
                message.success('Entrada criada com sucesso!');
                setFileList([]); // Limpa os arquivos após o sucesso
                navigate('/entradas'); // Redireciona para a lista de entradas
            } else {
                console.warn('Status inesperado:', response);
                message.error('Erro ao criar entrada.');
            }
        } catch (error) {
            console.error('Erro ao salvar:', error);
            if (error.response?.status === 413) {
                message.error('Um ou mais arquivos são muito grandes!');
            } else {
                message.error('Erro ao criar entrada. Verifique o console para mais detalhes.');
            }
        } finally {
            setLoading(false);
        }
    };

    const uploadButton = (
        <button
            style={{
                border: 0,
                background: 'none',
            }}
            type="button"
        >
            <PlusOutlined />
            <div style={{ marginTop: 8 }}>Upload</div>
        </button>
    );

    // Função para lidar com o cancelamento (voltar para a página de entradas)
    const handleCancelar = () => {
        navigate('/entradas');
    };

    return (
        <Layout style={{ minHeight: '100vh', width: '100vw' }}>
            <HeaderDiario />

            <Content
                style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    flexDirection: 'column',
                    padding: '3rem 2rem',
                    backgroundImage: `url(${backgroundImage})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    minHeight: 'calc(100vh - 64px - 80px)',
                    color: 'white',
                    paddingTop: '84px', // Distância do cabeçalho
                }}
            >
                <Card
                    style={{
                        width: '100%',
                        maxWidth: '600px',
                        padding: '1.5rem',
                        textAlign: 'left',
                    }}
                >
                    <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: 'white', textAlign: 'center', marginBottom: '1.5rem' }}>
                        Nova Entrada
                    </h1>

                    <Form layout="vertical" onFinish={onFinish}>
                        <Row gutter={[16, 16]}>
                            <Col span={24}>
                                <Form.Item
                                    label="Título"
                                    name="titulo"
                                    rules={[{ required: true, message: 'Digite o título da entrada' }]}
                                >
                                    <TextArea rows={1} placeholder="Adicione o título" />
                                </Form.Item>
                            </Col>
                            <Col span={24}>
                                <Form.Item
                                    label="Conteúdo"
                                    name="conteudo"
                                    rules={[{ required: true, message: 'Digite o conteúdo da entrada' }]}
                                >
                                    <TextArea rows={6} placeholder="Como foi seu dia?" />
                                </Form.Item>
                            </Col>
                            <Col span={24}>
                                <Form.Item label="Mídias (opcional)">
                                    <Upload
                                        listType="picture-card" // Estilo de lista de imagens/cartões
                                        fileList={fileList}
                                        onPreview={handlePreview} // Para exibir prévia de imagens
                                        onChange={handleChange} // Lida com adição/remoção de arquivos
                                        beforeUpload={() => false} // Evita upload automático do Ant Design
                                        multiple // Permite múltiplos arquivos
                                        accept="image/*,video/*,audio/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document" // Aceita múltiplos tipos
                                    >
                                        {fileList.length >= 8 ? null : uploadButton} {/* Limite de 8 arquivos */}
                                    </Upload>
                                    <Modal open={previewOpen} title={previewTitle} footer={null} onCancel={handlePreviewCancel}>
                                        <img alt="prévia" style={{ width: '100%' }} src={previewImage} />
                                    </Modal>
                                </Form.Item>
                            </Col>
                        </Row>

                        <Form.Item style={{ textAlign: 'right', marginTop: '1.5rem' }}>
                            <Button
                                type="primary"
                                htmlType="submit"
                                loading={loading}
                                icon={<SaveOutlined />}
                                style={{ marginRight: '10px' }}
                            >
                                Salvar Entrada
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

            <Footer style={{ textAlign: 'center', color: '#fff', backgroundColor: 'transparent', fontSize: '0.8rem' }}>
                © {new Date().getFullYear()} Powered by Jim Alves
            </Footer>
        </Layout>
    );
};

export default NovaEntrada;