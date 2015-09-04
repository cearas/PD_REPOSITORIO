package pd_trabalhopratico;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

class EnviaInformacaoUDP extends Thread
{
    GetRemoteClienteInfServiceInterface informacaotoServico;
    DatagramSocket socket;
    DatagramPacket packet;
    InformacaoDoRepositorio informacaodorepositorio;
    Informacao_a_Passar_ao_Servidor informacao_para_servidor;
    
    public EnviaInformacaoUDP(InformacaoDoRepositorio _informacaodorepositorio,DatagramSocket socket, DatagramPacket packet,GetRemoteClienteInfServiceInterface informacaotoServicox)
    {
        informacaotoServico=informacaotoServicox;
        informacao_para_servidor = new Informacao_a_Passar_ao_Servidor();
        informacaodorepositorio = _informacaodorepositorio;
        this.socket=socket;
        this.packet=packet;
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream() ;
                ObjectOutputStream oout;
                informacao_para_servidor.setListadeFicheiros(informacaodorepositorio.getListaFicheiros());
                informacao_para_servidor.setNumerodeLigacoes(informacaodorepositorio.getNumeroDeLigacoes());
                String aux = "Informacao";
                informacao_para_servidor.setComando(aux);
                sleep(10);
                oout = new ObjectOutputStream(out);
                oout.writeObject(informacao_para_servidor);
                //oout.flush();
                //String aux = "Informação";
                packet.setData(out.toByteArray());
                packet.setLength(out.toByteArray().length);
                socket.send(packet);
            } catch (InterruptedException ex) {
                Logger.getLogger(EnviaInformacaoUDP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(EnviaInformacaoUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class RecebePedidoClienteTCP extends Thread{

    ObjectOutputStream out;
    ObjectInputStream in;
    ServerSocket socket_repositorio = null;
    Socket socket_cliente;
    int port = 5003;
    InetAddress endereco_cliente;
    
    public String TriagemPedidoCliente(String opcaoCliente){
        
        if(Integer.parseInt(opcaoCliente) == 1){
            System.out.println("O utilizador pretende transferir um ficheiro");
            
            //Preparar para receber um ficheiro
            
            String mensagemCliente = "O ficheiro foi recebido com sucesso!";
            
            return mensagemCliente;
        }
        else if(Integer.parseInt(opcaoCliente) == 2){
            System.out.println("O utilizador pretende eliminar ficheiro!");
            
            //Preparar para eliminar um ficheiro
            
            String mensagemCliente = "O ficheiro foi eliminado com sucesso!";
            
            return mensagemCliente;
        }
        else{
            //Preparar para receber transferir um ficheiro
            System.out.println("Transferir ficheiro do repositorio!");   
        }
        
        return "operacao invalida";
    }
    
    @Override
    public void run() {
        
        while(true) {
            try {
                socket_repositorio = new ServerSocket(5003);
                socket_cliente = socket_repositorio.accept();
                System.out.println(socket_cliente.getInetAddress()+":"+socket_cliente.getPort()+" Conectou-se ao repositorio!");
                in = new ObjectInputStream(socket_cliente.getInputStream());
                
                String opcaoCliente = (String)in.readObject();
                String mensagemCliente = TriagemPedidoCliente(opcaoCliente);   
                
                out = new ObjectOutputStream(socket_cliente.getOutputStream());
                
                out.writeObject(mensagemCliente);

            } catch (IOException ex) {
                Logger.getLogger(EnviaInformacaoUDP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RecebePedidoClienteTCP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
class AtendePedidonoRepositorio extends Thread
{   Informacao_a_Passar_ao_Servidor inf_to_server;
    ObjectOutputStream oout;
    Metodos_Auxiliares_do_Repositorio metodos_auxiliares_repositorio;
    InformacaoDoRepositorio informacaorepositorio;
    String pastaRepositorio;
    Socket toCliente;
    ObjectInputStream inObjR;
    FileOutputStream ficheiroaguardar;
    InputStream inR;
    ByteArrayOutputStream Bout;
    DatagramPacket packet; 
    DatagramSocket socket;
    GetRemoteClienteInfServiceInterface informacaotoServico;
    int nbytes;
    byte [] fileChunck = new byte[512];
    
    public AtendePedidonoRepositorio(Socket x,String caminhox,InformacaoDoRepositorio informacaorepositoriox,DatagramPacket Packetx,DatagramSocket Socketx,GetRemoteClienteInfServiceInterface informacaotoServicox) {
        informacaotoServico = informacaotoServicox;
        socket = Socketx;
        packet = Packetx;
        metodos_auxiliares_repositorio = new Metodos_Auxiliares_do_Repositorio();
        toCliente = x;
        pastaRepositorio = caminhox;
        informacaorepositorio = informacaorepositoriox;
    }
    @Override
    public void run()
    {
        try {
            inObjR = new ObjectInputStream(toCliente.getInputStream());
            inR = toCliente.getInputStream();
            String cmd = (String)inObjR.readObject();
            File ficheiro = new File((String)inObjR.readObject());
            if(cmd.equalsIgnoreCase("transferir"))
            {
                ficheiroaguardar = new FileOutputStream(pastaRepositorio+File.separator+ficheiro.getName());
                while((nbytes = inR.read(fileChunck)) > 0){
                    ficheiroaguardar.write(fileChunck, 0, nbytes);
                }
                ficheiroaguardar.close();
                informacaorepositorio.DecrementaLigacao();
            }
            if(cmd.equalsIgnoreCase("carregar"))
            {
                byte[]filechunck = new byte[512];
                File file = new File(pastaRepositorio+File.separator+ficheiro.getName());
                FileInputStream fileToTransfer = new FileInputStream(file);
                OutputStream outR =toCliente.getOutputStream();
                //ObjectOutputStream outObjR = new ObjectOutputStream(toCliente.getOutputStream());
                while((nbytes = fileToTransfer.read(filechunck)) > 0){                    
                    outR.write(filechunck, 0, nbytes);
                    outR.flush();
                }
                toCliente.close();
                informacaorepositorio.DecrementaLigacao();
                return;
            }
            if(cmd.equalsIgnoreCase("apagar"))
            {
                File file = new File(pastaRepositorio+File.separator+ficheiro.getName());
                String path = file.getCanonicalPath();
                File filePath = new File(path);
                filePath.delete();
                informacaorepositorio.DecrementaLigacao();
            }   
                informacaorepositorio.ActualizaListaFicheiros(metodos_auxiliares_repositorio.actualiza_ficheiros_repositorio(pastaRepositorio));
                inf_to_server = new Informacao_a_Passar_ao_Servidor();

                inf_to_server.setComando("actualizacao");
                inf_to_server.setListadeFicheiros(informacaorepositorio.getListaFicheiros());
                inf_to_server.setNumerodeLigacoes(informacaorepositorio.getNumeroDeLigacoes());
                Bout = new ByteArrayOutputStream();
                oout = new ObjectOutputStream(Bout);
                oout.writeObject(inf_to_server);
                //oout.flush();
                //String aux = "Informação";
                DatagramPacket packtex = new DatagramPacket(Bout.toByteArray(),Bout.toByteArray().length,packet.getAddress(),packet.getPort());               
                socket.send(packtex);
                informacaotoServico.SetListaFicheiros(metodos_auxiliares_repositorio.carregarFiles(pastaRepositorio));
                if(cmd.equalsIgnoreCase("transferir"))
                {
                    inf_to_server.setComando("passar_ficheiro");
                    inf_to_server.setListadeFicheiros(informacaorepositorio.getListaFicheiros());
                    inf_to_server.setArgumentos_comando(pastaRepositorio+File.separator+ficheiro.getName());
                    Bout = new ByteArrayOutputStream();
                    oout = new ObjectOutputStream(Bout);
                    oout.writeObject(inf_to_server);
                    oout.flush();
                    packtex = new DatagramPacket(Bout.toByteArray(),Bout.toByteArray().length,packet.getAddress(),packet.getPort());
                    socket.send(packtex);
                }
                if(cmd.equalsIgnoreCase("apagar"))
                {
                    inf_to_server.setComando("apagar");
                    inf_to_server.setListadeFicheiros(informacaorepositorio.getListaFicheiros());
                    inf_to_server.setArgumentos_comando(pastaRepositorio+File.separator+ficheiro.getName());
                    Bout = new ByteArrayOutputStream();
                    oout = new ObjectOutputStream(Bout);
                    oout.writeObject(inf_to_server);
                    packet.setData(Bout.toByteArray());
                    packet.setLength(Bout.toByteArray().length);
                    socket.send(packet);
                }
                
                socket.receive(packet);
                if(cmd.equalsIgnoreCase("apagar"))
                {
                    ByteArrayInputStream BAinput = new ByteArrayInputStream(packet.getData(),0,packet.getLength());
                    ObjectInputStream OIS = new ObjectInputStream(BAinput);
                    InformacaoEnviarCliente informacaodoServidor= (InformacaoEnviarCliente)OIS.readObject();
                    if(informacaodoServidor.getComando().equalsIgnoreCase("check"))
                    {
                        System.out.println("Feito");
                    }
                    if(informacaodoServidor.getComando().equalsIgnoreCase("passa"))
                    {
                            Socket toOutroRepositorio=new Socket(informacaodoServidor.getAddress(),informacaodoServidor.getPortodeEscutaTCP());
                            informacaorepositorio.IncrementaLigacao();
                            OutputStream outR =toOutroRepositorio.getOutputStream();
                            ObjectOutputStream outObjR = new ObjectOutputStream(toOutroRepositorio.getOutputStream());
                            cmd = new String("apagar");
                            outObjR.writeObject(cmd);
                            outObjR.flush();
                            outObjR.writeObject(pastaRepositorio+File.separator+ficheiro.getName());
                            outObjR.flush();
                            toOutroRepositorio.close();
                            informacaorepositorio.DecrementaLigacao();
                    }
                }
                if(cmd.equalsIgnoreCase("transferir"))
                {
                    ByteArrayInputStream BAinput = new ByteArrayInputStream(packet.getData(),0,packet.getLength());
                    ObjectInputStream OIS = new ObjectInputStream(BAinput);
                    InformacaoEnviarCliente informacaodoServidor= (InformacaoEnviarCliente)OIS.readObject();
                    if(informacaodoServidor.getComando().equalsIgnoreCase("check"))
                    {
                        System.out.println("Feito");
                    }
                    if(informacaodoServidor.getComando().equalsIgnoreCase("passa"))
                    {
                            Socket toOutroRepositorio=new Socket(informacaodoServidor.getAddress(),informacaodoServidor.getPortodeEscutaTCP());
                            informacaorepositorio.IncrementaLigacao();
                            FileInputStream fileToTransfer = new FileInputStream(new File(pastaRepositorio+File.separator+ficheiro.getName()));
                            OutputStream outR =toOutroRepositorio.getOutputStream();
                            ObjectOutputStream outObjR = new ObjectOutputStream(toOutroRepositorio.getOutputStream());
                            cmd = new String("transferir");
                            outObjR.writeObject(cmd);
                            outObjR.flush();
                            outObjR.writeObject(pastaRepositorio+File.separator+ficheiro.getName());
                            outObjR.flush();
                            while((nbytes = fileToTransfer.read(fileChunck)) > 0){                    
                                outR.write(fileChunck, 0, nbytes);
                                outR.flush();
                            }
                            toOutroRepositorio.close();
                            informacaorepositorio.DecrementaLigacao();
                    }
            }
        } catch (IOException ex) {
            System.out.println("Impossivel registar Repositorio par escuta : "+ex.getMessage());} catch (ClassNotFoundException ex) {
            System.out.println("Classe não identificada"+ex.getMessage());
        }
    }
}
public class Repositorio {
    
    public static void main(String[]args)
    {
    	int port=5003;
        
        String pastadeficheiros = "C:\\Users\\Ceara\\Documents\\NetBeansProjects\\PD_TrabalhoPratico\\Rep"; 
    	
        GetRemoteClienteInfServiceInterface informacaotoServico;
        String objectUrl;
        AtendePedidonoRepositorio atendepedido;
        Informacao_a_Passar_ao_Servidor informacao_para_servidor;
        Metodos_Auxiliares_do_Repositorio metodos_auxiliares_repositorio;
        ServerSocket socket; 
        Socket SocketToCliente_repositorio;
        InformacaoDoRepositorio informacaorepositorio;
        InformacaoServidor informacao_servidor = null;
        DatagramPacket packet;
        DatagramSocket socket_servidor = null;
        String aux;
        InetAddress endereco_servidor = null;
        
        try {
            objectUrl = "rmi://127.0.0.1/GetInformacaoServidor";
            informacaotoServico = (GetRemoteClienteInfServiceInterface)Naming.lookup(objectUrl);
            metodos_auxiliares_repositorio = new Metodos_Auxiliares_do_Repositorio();
            informacao_para_servidor = new Informacao_a_Passar_ao_Servidor();
            
            socket = new ServerSocket(0);
            System.out.println("Meu porto de escuta -> "+socket.getLocalPort());
            informacaorepositorio = new InformacaoDoRepositorio(socket.getLocalPort(),socket.getInetAddress());
            informacaorepositorio.ActualizaListaFicheiros(metodos_auxiliares_repositorio.actualiza_ficheiros_repositorio(pastadeficheiros));
            informacao_para_servidor.setPort_escuta_TCP(socket.getLocalPort());
            informacao_para_servidor.setListadeFicheiros(informacaorepositorio.ListadeFicheiros);
            informacao_para_servidor.setPasta_Repositorio(pastadeficheiros);
            aux = "informacao";
            informacao_para_servidor.setComando(aux);
            informacao_para_servidor.setNumerodeLigacoes(0);
            endereco_servidor = InetAddress.getByName("localhost");
            informacao_servidor = new InformacaoServidor(endereco_servidor,port,null);
            socket_servidor = new DatagramSocket();
            
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            ObjectOutputStream oout;
            oout = new ObjectOutputStream(out);
            oout.writeObject(informacao_para_servidor);
            packet = new DatagramPacket(out.toByteArray(),out.toByteArray().length,informacao_servidor.getAddress(),informacao_servidor.getPort());
            socket_servidor.send(packet);
            informacaotoServico.SetListaFicheiros(metodos_auxiliares_repositorio.carregarFiles(pastadeficheiros));
            EnviaInformacaoUDP envia_informacao = new EnviaInformacaoUDP(informacaorepositorio,socket_servidor, packet,informacaotoServico);
            envia_informacao.setDaemon(true);
            envia_informacao.start();
            do
            {
                SocketToCliente_repositorio = socket.accept();
                informacaorepositorio.IncrementaLigacao();
                System.out.println("Processo "+ SocketToCliente_repositorio.getInetAddress()+":"+ SocketToCliente_repositorio.getPort()+" Ligouse ao Repositorio");
                atendepedido = new AtendePedidonoRepositorio(SocketToCliente_repositorio,pastadeficheiros,informacaorepositorio,packet,socket_servidor,informacaotoServico);
                atendepedido.setDaemon(true);
                atendepedido.start();
            }while(envia_informacao.isAlive());
        } 
        catch (UnknownHostException ex) 
        {
            System.out.println("Erro ao atribuir o InetAddress :"+ex.getMessage());
        } catch (SocketException ex) {
            System.out.println("Não conseguiu abrir DatagramSocket: "+ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Erro ao enviar DatagramPacket:"+ex.getMessage());
        } catch (NotBoundException ex) {
            Logger.getLogger(Repositorio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}