import React from 'react';
import { Layout, Card, Button, Typography } from 'antd'; 
import { FileAddOutlined, FileTextOutlined } from '@ant-design/icons'; 
import { useNavigate } from 'react-router-dom';
import backgroundImage from '../assets/background.jpg';
import HeaderDiario from '../components/HeaderDiario'; 

const { Content, Footer } = Layout;
const { Title, Paragraph } = Typography; 

const Home = () => {
    const navigate = useNavigate();

    return (
        <Layout style={{ minHeight: '100vh', width: '100vw' }}>
            <HeaderDiario /> 

            {/* Conteúdo com a imagem de fundo */}
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
                    paddingTop: '84px', // Distância do cabeçalho
                }}
            >
                <Card
                    style={{
                        width: '100%', 
                        maxWidth: '450px', 
                        padding: '2rem', 
                        textAlign: 'center', 
                    }}
                >
                    <Title level={1} style={{ color: 'inherit', fontWeight: 'bold', marginBottom: '1rem' }}>
                        Bem-vindo <br/>ao seu Diário!
                    </Title>

                    <Paragraph style={{ color: 'inherit', fontSize: '1rem', marginBottom: '2rem' }}>
                        Acesse suas entradas <br/>ou crie novas memórias.
                    </Paragraph>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <Button
                            type="primary"
                            icon={<FileTextOutlined />}
                            onClick={() => navigate('/entradas')}
                            size="large" 
                        >
                            Minhas Entradas
                        </Button>
                        <Button
                            type="default" 
                            icon={<FileAddOutlined />}
                            onClick={() => navigate('/entradas/nova')}
                            size="large"
                        >
                            Nova Entrada
                        </Button>
                    </div>
                </Card>
            </Content>

            <Footer style={{ textAlign: 'center', color: '#fff', backgroundColor: 'transparent', fontSize: '0.8rem' }}>
                © {new Date().getFullYear()} Powered by Jim Alves
            </Footer>
        </Layout>
    );
};

export default Home;
