import React from 'react';
import { Layout, Menu, Button } from 'antd';
import { LogoutOutlined, FileAddOutlined, FileTextOutlined, HomeOutlined } from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom'; 

const { Header } = Layout;

const HeaderDiario = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('usuarioId');
        navigate('/'); 
    };

    const getSelectedKey = () => {
        if (location.pathname === '/home') {
            return 'home';
        }
        if (location.pathname.startsWith('/entradas') && !location.pathname.startsWith('/entradas/nova')) {
            return 'listar';
        }
        if (location.pathname === '/entradas/nova') {
            return 'nova';
        }
        
        return 'home'; 
    };

    return (
        <Header
            style={{ 
                padding: '0 20px', 
                position: 'fixed', 
                top: 0,           
                width: '100%',    
                zIndex: 1000,     
                height: '64px',   
                backgroundColor: '#001529', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'space-between', 
            }} 
        >
            <div 
                style={{ 
                    color: 'white', 
                    fontSize: '1.5rem', 
                    fontWeight: 'bold', 
                    cursor: 'pointer',
                    whiteSpace: 'nowrap', 
                }}
                onClick={() => navigate('/home')} 
            >
                Meu Diário
            </div>
            
            <Menu
                theme="dark" 
                mode="horizontal"
                selectedKeys={[getSelectedKey()]}
                onSelect={({ key }) => navigate(`/${key === 'home' ? 'home' : (key === 'listar' ? 'entradas' : 'entradas/nova')}`)} 
                style={{ 
                    flexGrow: 1, 
                    borderBottom: 'none', 
                    lineHeight: '64px', 
                    backgroundColor: 'transparent', 
                    marginLeft: '20px', 
                }} 
            >
                <Menu.Item key="home" icon={<HomeOutlined />}>
                    Início
                </Menu.Item>
                <Menu.Item
                    key="listar"
                    icon={<FileTextOutlined />}
                >
                    Minhas Entradas
                </Menu.Item>
                <Menu.Item
                    key="nova"
                    icon={<FileAddOutlined />}
                >
                    Nova Entrada
                </Menu.Item>
            </Menu>

            <div style={{ marginLeft: 'auto' }}> 
                <Button
                    type="primary" 
                    icon={<LogoutOutlined />}
                    onClick={logout}
                    style={{ backgroundColor: 'red', borderColor: 'red' }} 
                >
                    Sair
                </Button>
            </div>
        </Header>
    );
};

export default HeaderDiario;