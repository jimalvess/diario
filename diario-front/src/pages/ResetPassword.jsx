import React, { useState, useEffect } from 'react';
import { Form, Input, Button, message, Card, Modal, Layout, Typography } from 'antd'; 
import { useLocation, useNavigate } from 'react-router-dom';
import backgroundImage from '../assets/background.jpg';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

const ResetPassword = () => {
  const [loading, setLoading] = useState(false);
  const [token, setToken] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false); 
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const tokenFromUrl = queryParams.get('token');
    if (tokenFromUrl) {
      setToken(tokenFromUrl);
    } else {
      message.error('Token de redefinição de senha não encontrado.');
      navigate('/login');
    }
  }, [location, navigate]);

  const onFinish = async (values) => {
    try {
      setLoading(true);

      const response = await fetch('http://localhost:8080/auth/redefinir-senha', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token,
          novaSenha: values.senha,
        }),
      });

      if (response.ok) {
        setIsModalVisible(true);
      } else {
        message.error('Erro ao redefinir a senha.');
      }
    } catch (error) {
      console.error('Erro de conexão ao redefinir senha:', error); // Log mais detalhado pra mim
      message.error('Erro de conexão.');
    } finally {
      setLoading(false);
    }
  };

  const handleOk = () => {
    setIsModalVisible(false);

    // Delay para que o modal feche antes de redirecionar
    setTimeout(() => {
      console.log('Redirecionando para /login...');
      navigate('/login');
    }, 300);
  };

  return (
    <Layout style={{ minHeight: '100vh', width: '100vw' }}>
      <Content
        style={{
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        <Card
          style={{
            width: '100%',
            maxWidth: 400, 
            padding: '1.5rem', 
            textAlign: 'center', 
          }}
          title={
            <Title level={3} style={{ color: 'inherit', margin: 0 }}>
              Redefinir Senha
            </Title>
          }
        >
          <Form name="reset-password-form" onFinish={onFinish} layout="vertical">
            <Form.Item
              name="senha"
              rules={[{ required: true, message: 'Por favor, insira a nova senha!' }]}
              hasFeedback // Adiciona feedback visual pra validação
            >
              <Input.Password placeholder="Nova Senha" />
            </Form.Item>

            <Form.Item
              name="confirmarSenha"
              dependencies={['senha']} // Depende do campo 'senha'
              hasFeedback // Adiciona feedback visual pra validação
              rules={[
                { required: true, message: 'Por favor, confirme a nova senha!' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('senha') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject('As senhas não correspondem!');
                  },
                }),
              ]}
            >
              <Input.Password placeholder="Confirmar Senha" />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0 }}>
              <Button type="primary" htmlType="submit" block loading={loading} size="large">
                Redefinir Senha
              </Button>
            </Form.Item>
          </Form>
        </Card>

        {/* Modal de Sucesso */}
        <Modal
          title={<Title level={4} style={{ color: 'inherit', margin: 0 }}>Senha redefinida com sucesso!</Title>}
          open={isModalVisible} 
          onOk={handleOk}
          onCancel={handleOk}
          okText="Fazer login"
          cancelButtonProps={{ style: { display: 'none' } }} // Oculta o botão Cancelar
        >
          <Paragraph style={{ color: 'inherit', marginBottom: '1rem' }}>
            Agora você pode fazer login com sua nova senha.
          </Paragraph>
        </Modal>
      </Content>
    </Layout>
  );
};

export default ResetPassword;
