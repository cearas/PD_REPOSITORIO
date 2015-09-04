package pd_trabalhopratico;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class EnviaFicheiroRepositorioTCP{

    private String nomeFicheiro;
    private InetAddress enderecoRepositorio;
    
    public EnviaFicheiroRepositorioTCP(String nomeFicheiro, InetAddress enderecoRepositorio) {
        this.nomeFicheiro = nomeFicheiro;
        this.enderecoRepositorio = enderecoRepositorio;
    }
    
    public boolean verificaficheiro(String caminho)
    {
        File x = new File(caminho);
        if(!x.exists())
        {
            System.out.println("Ficheiro não existe");
            return false;
        }
        else
        {
            if(x.isFile())
            {
                if(x.canRead() && x.canWrite())
                {
                    return true;
                }
                else
                {
                    System.out.println("Ficherio não se pode ler/escrever");
                    return false;
                }
            }
            else
            {
                System.out.println("Caminho indicado não é um ficheiro");
                return false;
            }
        }     
    }

    public void EnviaFicheiro(){
        System.out.println("A enviar ficheiro");
        
        //Fazer o pedido TCP e enviar o ficheiro
    }
}
class AtendePedidos extends Thread
{
    GetRemoteClienteInfServiceInterface informacaotoServico;
    InformacaoCliente minhainformacao;
    ClassesAuxiliares auxiliares;
    ObjectInputStream in;
    InformacaoEnviarCliente infx;
    Socket toRepositorio;
    int nbytes;
    byte[]filechunck = new byte[512];
    public AtendePedidos(ObjectInputStream inx,InformacaoCliente minhainformacaox,GetRemoteClienteInfServiceInterface informacaotoServicox)
    {
        informacaotoServico=informacaotoServicox;
        minhainformacao=minhainformacaox;
        auxiliares= new ClassesAuxiliares();
        in = inx;
    }
    @Override
    public void run()
    {
        while(true)
        {
            try {
                infx = (InformacaoEnviarCliente)in.readObject();
                if(infx.getComando().equalsIgnoreCase("lista"))
                {
                    if(infx.getListaficheiro().size()>0)
                    {
                        System.out.println("***************Lista de Ficheiros**************");
                        for(int i = 0; i< infx.getListaficheiro().size();i++)
                        {
                            System.out.println("-> "+infx.getListaficheiro().get(i));
                        }
                        auxiliares.Apresenta_menu_cliente();
                    }
                }
                if(infx.getComando().equalsIgnoreCase("erro"))
                {
                    System.out.println("Erro -> "+infx.getArgumentos());
                }
                if(infx.getComando().equalsIgnoreCase("transferir"))
                {
                    toRepositorio=new Socket(infx.getAddress(),infx.getPortodeEscutaTCP());
                    FileInputStream fileToTransfer = new FileInputStream(new File(infx.getArgumentos()));
                    OutputStream outR =toRepositorio.getOutputStream();
                    ObjectOutputStream outObjR = new ObjectOutputStream(toRepositorio.getOutputStream());
                    String cmd = new String("transferir");
                    outObjR.writeObject(cmd);
                    outObjR.flush();
                    outObjR.writeObject(infx.getArgumentos());
                    outObjR.flush();
                    while((nbytes = fileToTransfer.read(filechunck)) > 0){                    
                        outR.write(filechunck, 0, nbytes);
                        outR.flush();
                    }
                    toRepositorio.close();
                    informacaotoServico.SetCliente(0,minhainformacao.getNome(),minhainformacao.getPass(),minhainformacao.getInet(),minhainformacao.getPort(),null);
                }
                if(infx.getComando().equalsIgnoreCase("apagar"))
                {
                    toRepositorio=new Socket(infx.getAddress(),infx.getPortodeEscutaTCP());
                    OutputStream outR =toRepositorio.getOutputStream();
                    ObjectOutputStream outObjR = new ObjectOutputStream(toRepositorio.getOutputStream());
                    String cmd = new String("apagar");
                    outObjR.writeObject(cmd);
                    outObjR.flush();
                    outObjR.writeObject(infx.getArgumentos());
                    outObjR.flush();
                    toRepositorio.close();
                    informacaotoServico.SetCliente(0,minhainformacao.getNome(),minhainformacao.getPass(),minhainformacao.getInet(),minhainformacao.getPort(),null);
                }
                if(infx.getComando().equalsIgnoreCase("carregar"))
                {
                    toRepositorio=new Socket(infx.getAddress(),infx.getPortodeEscutaTCP());
                    OutputStream outR =toRepositorio.getOutputStream();
                    ObjectOutputStream outObjR = new ObjectOutputStream(toRepositorio.getOutputStream());
                    String cmd = new String("carregar");
                    outObjR.writeObject(cmd);
                    outObjR.flush();
                    String[]args = infx.getArgumentos().split(" ");
                    outObjR.writeObject(args[0]);
                    outObjR.flush();
                    InputStream inR =toRepositorio.getInputStream();
                    FileOutputStream ficheiroaguardar = new FileOutputStream(args[1]+File.separator+args[0]);
                    byte[]filechunck = new byte[512];
                    while((nbytes = inR.read(filechunck)) > 0){
                        ficheiroaguardar.write(filechunck, 0, nbytes);
                    }
                    ficheiroaguardar.close();
                    informacaotoServico.SetCliente(0,minhainformacao.getNome(),minhainformacao.getPass(),minhainformacao.getInet(),minhainformacao.getPort(),null);
                }
            } catch (IOException ex) {
                Logger.getLogger(AtendePedidos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AtendePedidos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
public class Cliente 
{
    public static String PedeLoginUtilizador(){
        
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira Username ->");
        String aux = sc.next().trim();
        System.out.println("Insira Password ->");
        return aux+" "+sc.next().trim();
    }
        
    public static void main(String args[])
    {
        GetRemoteClienteInfServiceInterface informacaotoServico;
        String objectUrl;
        objectUrl = "rmi://127.0.0.1/GetInformacaoServidor";   
        InformacaoCliente minhainformacao;
        Metodos_Auxiliares_do_Repositorio ma = new Metodos_Auxiliares_do_Repositorio();
        AtendePedidos atendepedidos;
        ObjectOutputStream out;
        ObjectInputStream in;
        InformacaoServidor informacao_servidor = null; 
        Socket socket_servidor = null;
        int port = 5003;
        InetAddress endereco_servidor;
        ClassesAuxiliares auxiliares;

        try {
            informacaotoServico = (GetRemoteClienteInfServiceInterface)Naming.lookup(objectUrl);
            endereco_servidor = InetAddress.getByName("localhost");
            informacao_servidor = new InformacaoServidor(endereco_servidor, port,null);
            socket_servidor = new Socket(informacao_servidor.getAddress(),port);
            informacao_servidor.setSokcet(socket_servidor);
            out = new ObjectOutputStream(socket_servidor.getOutputStream());
            
            out.writeObject(PedeLoginUtilizador());
            out.flush();
            in = new ObjectInputStream(socket_servidor.getInputStream());
            boolean estado_conexao = (boolean)in.readObject();
            if(estado_conexao){
                minhainformacao = (InformacaoCliente)in.readObject();
                Scanner input = new Scanner(System.in);
                atendepedidos = new AtendePedidos(in,minhainformacao,informacaotoServico);
                atendepedidos.setDaemon(true);
                atendepedidos.start();
                System.out.println("Login com sucesso!");
                while(true)
                {
                     String operacao;
                     auxiliares = new ClassesAuxiliares();
                     do{
                        auxiliares.Apresenta_menu_cliente();
                        operacao = input.next().trim();
                     if(Integer.parseInt(operacao)<1 || Integer.parseInt(operacao)>3)
                         System.out.println("Opção não dispinivel");
                     }while(Integer.parseInt(operacao)<1 || Integer.parseInt(operacao)>3);
                     
                     switch(Integer.parseInt(operacao)){
                         case 1:
                            System.out.println("Indique qual o ficheiro (caminho ex C:\\ ...\\nomedo.ficheiro.extensão)");
                            String nomeFicheiro = input.next().trim();
                            operacao+=(" "+nomeFicheiro);
                            //System.out.println(operacao);
                            EnviaFicheiroRepositorioTCP enviaFicheiro_1 = new EnviaFicheiroRepositorioTCP(nomeFicheiro, null);
                            if(enviaFicheiro_1.verificaficheiro(nomeFicheiro))
                            {
                                out.writeObject(operacao);
                                out.flush();
                            }
                        break;
                        case 2:
                            System.out.println("Indique qual o ficheiro a eliminar");
                            nomeFicheiro = input.next().trim();
                            operacao+=(" "+nomeFicheiro);
                            out.writeObject(operacao);
                            out.flush();
                        break;
                         case 3:
                            System.out.println("Indique qual o ficheiro a carregar");
                            nomeFicheiro = input.next().trim();
                            System.out.println("Indique pasta onde guardar o ficheiro");
                            String pastaFicheiro = input.next().trim();
                            if(ma.paginaExiste(pastaFicheiro))
                            {
                                operacao+=(" "+nomeFicheiro+" "+pastaFicheiro);
                                out.writeObject(operacao);
                                out.flush();
                            }
                        break;
                         
                     }
                 }
            }
            else{
                System.out.println("Login invalido");
            }
        } catch (IOException ex) {
            System.out.println("Erro -> "+ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("Erro ao ler objecto "+ex.getMessage());
        } catch (NotBoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
