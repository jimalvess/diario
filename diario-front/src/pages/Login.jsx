import React, { useState } from 'react';
import { Form, Input, Button, message, Card, Space, Modal, Layout, Typography } from 'antd'; 
import { login } from '../services/authService';
import backgroundImage from '../assets/background.jpg';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

const Login = () => {
  const [isRegistering, setIsRegistering] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [email, setEmail] = useState("");

  const onFinishLogin = async (values) => {
    try {
      const data = await login(values.username, values.senha);
      localStorage.setItem('token', data.token);
      localStorage.setItem('usuarioId', data.usuarioId);
      message.success('Login realizado com sucesso!');
      window.location.href = '/home';
    } catch (error) {
      message.error('Usuário ou senha inválidos!');
    }
  };

  const onFinishRegister = async (values) => {
    try {
      const response = await fetch('http://localhost:8080/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: values.username,
          senha: values.senha,
        }),
      });
      if (response.ok) {
        message.success('Cadastro realizado com sucesso!');
        setIsRegistering(false);
      } else {
        message.error('Erro ao registrar usuário!');
      }
    } catch (error) {
      message.error('Erro de conexão!');
    }
  };

  const handleSendResetLink = async () => {
    try {
      const response = await fetch('http://localhost:8080/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email }),
      });

      if (response.ok) {
        message.success('Link de redefinição de senha enviado para o seu e-mail!');
        setIsModalVisible(false);
      } else {
        message.error('Erro ao enviar link de redefinição de senha!');
      }
    } catch (error) {
      message.error('Erro de conexão!');
    }
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
            maxWidth: 350, 
            padding: '1.5rem', 
            textAlign: 'center', 
          }}
          title={
            <Title level={3} style={{ color: 'inherit', margin: 0 }}>
              {isRegistering ? 'Registrar-se' : 'Login'}
            </Title>
          }
        >
          <Form
            name="auth-form"
            onFinish={isRegistering ? onFinishRegister : onFinishLogin}
            layout="vertical" 
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: 'Por favor, digite seu usuário!' }]}
            >
              <Input placeholder="Usuário" />
            </Form.Item>

            <Form.Item
              name="senha"
              rules={[{ required: true, message: 'Por favor, digite sua senha!' }]}
            >
              <Input.Password placeholder="Senha" />
            </Form.Item>

            <Form.Item style={{ marginBottom: '1rem' }}> 
              <Button type="primary" htmlType="submit" block size="large">
                {isRegistering ? 'Registrar' : 'Entrar'}
              </Button>
            </Form.Item>

            <Form.Item style={{ marginBottom: 0 }}> 
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button
                  type="default"
                  onClick={() => setIsRegistering(!isRegistering)}
                  block 
                  style={{ padding: 0 }} 
                >
                  {isRegistering ? 'Já tem uma conta? Fazer login' : 'Não tem uma conta? Registrar'}
                </Button>

                {!isRegistering && (
                  <Button
                    type="default"
                    onClick={() => setIsModalVisible(true)}
                    block 
                    style={{ padding: 0 }} 
                  >
                    Esqueci minha senha
                  </Button>
                )}
              </Space>
            </Form.Item>
          </Form>
        </Card>

        {/* Modal de Recuperação de Senha */}
        <Modal
          title={<Title level={4} style={{ color: 'inherit', margin: 0 }}>Recuperação de Senha</Title>}
          open={isModalVisible} // 'visible' foi substituído por 'open' no Ant v5
          onCancel={() => setIsModalVisible(false)}
          onOk={handleSendResetLink}
          okText="Enviar Link"
          cancelText="Cancelar"
        >
          <Paragraph style={{ color: 'inherit', marginBottom: '1rem' }}>
            Digite seu e-mail para receber o link de redefinição de senha:
          </Paragraph>
          <Input
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="E-mail"
            type="email"
          />
        </Modal>
      </Content>
    </Layout>
  );
};

export default Login;
