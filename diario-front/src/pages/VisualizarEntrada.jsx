import React, { useState, useEffect } from 'react';
import {
    Layout,
    Button,
    message,
    Card,
    Typography,
    Image,
    Popconfirm,
    Modal, 
    Tag,   
} from 'antd';
import {
    EditOutlined,
    DeleteOutlined,
    ArrowLeftOutlined,
    EyeOutlined, 
}
from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import backgroundImage from '../assets/background.jpg';
import HeaderDiario from '../components/HeaderDiario'; 

const { Content, Footer } = Layout;
const { Title, Paragraph } = Typography; 

// Alturas exatas dos seus componentes
const HEADER_HEIGHT = 64; 
const FOOTER_HEIGHT = 67; 

const VisualizarEntrada = () => {
    const [entrada, setEntrada] = useState(null);
    const [loading, setLoading] = useState(true);
    const [previewOpen, setPreviewOpen] = useState(false); 
    const [previewMediaUrl, setPreviewMediaUrl] = useState(''); 
    const [previewTitle, setPreviewTitle] = useState(''); 
    const navigate = useNavigate();
    const { id } = useParams(); 
    const token = localStorage.getItem('token');
    const API_BASE_URL = 'http://localhost:8080'; 

    const handlePreviewCancel = () => {
        setPreviewOpen(false);
        setPreviewMediaUrl('');
        setPreviewTitle('');
    };

    const handleMediaPreview = (url, name) => {
        setPreviewMediaUrl(url);
        setPreviewTitle(name);
        setPreviewOpen(true);
    };

    useEffect(() => {
        const fetchEntrada = async () => {
            setLoading(true);
            try {
                if (!token) {
                    throw new Error('Token de autenticação não encontrado.');
                }

                const entradaResponse = await axios.get(`${API_BASE_URL}/api/entradas/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });

                setEntrada(entradaResponse.data); 

            } catch (error) {
                console.error('Erro ao carregar a entrada:', error);
                message.error('Erro ao carregar a entrada.');
                navigate('/entradas'); 
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchEntrada();
        } else {
            message.error('ID da entrada não fornecido.');
            navigate('/entradas');
        }
    }, [id, token, navigate, API_BASE_URL]); 

    const handleEditar = () => {
        navigate(`/entradas/editar/${id}`); 
    };

    const handleExcluir = async () => {
        try {
            const response = await axios.delete(`${API_BASE_URL}/api/entradas/${id}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (response.status === 204) { 
                message.success('Entrada excluída com sucesso.');
                navigate('/entradas'); 
            } else {
                console.warn('Status inesperado:', response.status);
                message.error('Erro ao excluir entrada.');
            }
        } catch (error) {
            console.error('Erro ao excluir:', error);
            if (error.response?.status === 403) {
                message.error('Você não tem permissão para excluir esta entrada.');
            } else if (error.response?.status === 404) {
                message.error('Entrada não encontrada para exclusão.');
            } else {
                message.error('Erro ao excluir entrada.');
            }
        }
    };

    const handleVoltar = () => {
        navigate('/entradas'); 
    };

    if (loading) {
        return (
            <Layout style={{ minHeight: '100vh', width: '100vw', display: 'flex', justifyContent: 'center', alignItems: 'center', backgroundImage: `url(${backgroundImage})`, backgroundSize: 'cover' }}>
                <p style={{ color: 'white', fontSize: '1.2rem', padding: '20px', backgroundColor: 'rgba(0,0,0,0.7)', borderRadius: '8px' }}>
                    Carregando entrada...
                </p>
            </Layout>
        );
    }

    if (!entrada) {
        return (
            <Layout style={{ minHeight: '100vh', width: '100vw', display: 'flex', justifyContent: 'center', alignItems: 'center', backgroundImage: `url(${backgroundImage})`, backgroundSize: 'cover' }}>
                    <p style={{ color: 'red', fontSize: '1.2rem', padding: '20px', backgroundColor: 'rgba(0,0,0,0.7)', borderRadius: '8px' }}>
                    Entrada não encontrada ou ocorreu um erro.
                </p>
            </Layout>
        );
    }

    return (
        // O Layout principal deve preencher a tela e ser um flex container para seus filhos
        <Layout style={{ minHeight: '100vh', width: '100vw', display: 'flex', flexDirection: 'column' }}> 
            {/* HeaderDiario já está configurado para ser fixo e ter a altura correta */}
            <HeaderDiario /> 

            {/* O Content agora tem a função principal de ser o container rolável */}
            <Content
                style={{
                    // paddingTop compensa a altura do header fixo + um espaço extra
                    paddingTop: `${HEADER_HEIGHT + 30}px`, 
                    paddingLeft: '2rem',
                    paddingRight: '2rem',
                    paddingBottom: '3rem', 
                    
                    backgroundImage: `url(${backgroundImage})`, 
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    backgroundAttachment: 'fixed', 
                    
                    flex: 1, // Faz com que o Content ocupe todo o espaço vertical disponível
                    color: 'white', 
                    overflowY: 'auto', // ESSENCIAL: Garante que o Content seja rolável
                    
                    display: 'flex', 
                    flexDirection: 'column', 
                    alignItems: 'center', // Centraliza o Card horizontalmente
                    // Removemos justifyContent para que o conteúdo comece do topo do Content
                }}
            >
                <Card
                    style={{
                        width: '100%', 
                        maxWidth: '700px', 
                        padding: '1.5rem', 
                        textAlign: 'left', 
                        backgroundColor: 'rgba(0, 0, 0, 0.6)', 
                        boxShadow: '0px 4px 15px rgba(0, 0, 0, 0.5)',
                        borderRadius: '8px',
                        // Margin auto para centralização horizontal dentro do Content flex
                        margin: '0 auto', 
                        marginBottom: '30px', 
                    }}
                >
                    <Title level={1} style={{ color: '#ddbf13', textAlign: 'center', marginBottom: '1.5rem' }}>
                        {entrada.titulo}
                    </Title>

                    <Title level={4} style={{ color: 'white', marginBottom: '0.5rem' }}>
                        Data: {new Date(entrada.data).toLocaleDateString('pt-BR')}
                    </Title>
                    <Paragraph style={{ color: 'white', whiteSpace: 'pre-wrap', marginBottom: '1.5rem' }}>
                        {entrada.conteudo}
                    </Paragraph>

                    {entrada.midias && entrada.midias.length > 0 && (
                        <div style={{ marginTop: '1.5rem', borderTop: '1px solid rgba(255,255,255,0.2)', paddingTop: '1.5rem' }}>
                            <Title level={5} style={{ color: 'white', marginBottom: '1rem' }}>Mídias Anexadas:</Title>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', justifyContent: 'center' }}>
                                {entrada.midias.map(media => {
                                    const mediaFileName = media.caminhoArquivo
                                        ? media.caminhoArquivo.split(/[\\/]/).pop() 
                                        : '';
                                    const mediaUrl = `${API_BASE_URL}/api/entradas/arquivo/${mediaFileName}`;
                                    const isImage = media.tipoArquivo === 'imagem';
                                    const isVideo = media.tipoArquivo === 'video';
                                    const isAudio = media.tipoArquivo === 'audio';

                                    return (
                                        <Card
                                            key={media.id}
                                            size="small"
                                            style={{ width: 140, textAlign: 'center', backgroundColor: 'rgba(255,255,255,0.1)', borderColor: '#555', color: 'white' }}
                                            bodyStyle={{ padding: '8px' }}
                                            cover={
                                                isImage ? (
                                                    <Image
                                                        alt={media.nomeOriginalArquivo}
                                                        src={mediaUrl}
                                                        style={{ height: 90, objectFit: 'contain', padding: '5px' }}
                                                        preview={{ src: mediaUrl }} 
                                                    />
                                                ) : isVideo ? (
                                                    <video controls style={{ height: 90, width: '100%', objectFit: 'contain' }}>
                                                        <source src={mediaUrl} type={`${media.tipoArquivo}/${media.nomeOriginalArquivo.split('.').pop()}`} />
                                                        Seu navegador não suporta a tag de vídeo.
                                                    </video>
                                                ) : isAudio ? (
                                                    <audio controls style={{ width: '100%', marginTop: '5px' }}>
                                                        <source src={mediaUrl} type={`${media.tipoArquivo}/${media.nomeOriginalArquivo.split('.').pop()}`} />
                                                        Seu navegador não suporta a tag de áudio.
                                                    </audio>
                                                ) : ( // Para documentos
                                                    <div
                                                        style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: 90, color: 'white', cursor: 'pointer' }}
                                                        onClick={() => handleMediaPreview(mediaUrl, media.nomeOriginalArquivo)}
                                                    >
                                                        <EyeOutlined style={{ fontSize: '30px', color: '#1890ff' }} />
                                                        <Tag color="blue" style={{ marginTop: '5px' }}>{media.tipoArquivo.replace('_', ' ')}</Tag>
                                                    </div>
                                                )
                                            }
                                        >
                                            <Card.Meta title={<span style={{ color: 'white', fontSize: '0.8rem' }}>{media.nomeOriginalArquivo}</span>} />
                                        </Card>
                                    );
                                })}
                            </div>
                        </div>
                    )}

                    <div style={{ marginTop: '2rem', textAlign: 'center' }}>
                        <Button
                            type="primary"
                            icon={<EditOutlined />}
                            onClick={handleEditar}
                            style={{ marginRight: '10px' }}
                        >
                            Editar
                        </Button>
                        <Popconfirm
                            title="Tem certeza que deseja excluir esta entrada?"
                            onConfirm={handleExcluir}
                            okText="Sim"
                            cancelText="Não"
                        >
                            <Button
                                type="danger" 
                                icon={<DeleteOutlined />}
                                style={{ marginRight: '10px' }}
                            >
                                Excluir
                            </Button>
                        </Popconfirm>
                        <Button
                            type="default" 
                            icon={<ArrowLeftOutlined />}
                            onClick={handleVoltar}
                        >
                            Voltar
                        </Button>
                    </div>
                </Card>
            </Content>

            {/* Footer com altura definida para que o Content possa calcular seu espaço */}
            <Footer style={{ textAlign: 'center', color: '#fff', backgroundColor: 'transparent', fontSize: '0.8rem', height: `${FOOTER_HEIGHT}px` }}>
                © {new Date().getFullYear()} Powered by Jim Alves
            </Footer>

            <Modal open={previewOpen} title={previewTitle} footer={null} onCancel={handlePreviewCancel} width="80%">
                {previewMediaUrl && (
                    console.log("DEBUG: Renderizando iframe com src:", previewMediaUrl), 
                    previewMediaUrl.includes('.pdf') ? (
                        <iframe src={previewMediaUrl} style={{ width: '100%', height: '70vh', border: 'none' }} />
                    ) : (
                        <div style={{ textAlign: 'center' }}>
                            <p>Prévia não disponível para este tipo de arquivo diretamente no navegador. Tente baixar:</p>
                            <a href={previewMediaUrl} target="_blank" rel="noopener noreferrer">Abrir {previewTitle}</a>
                        </div>
                    )
                )}
            </Modal>
        </Layout>
    );
};

export default VisualizarEntrada;