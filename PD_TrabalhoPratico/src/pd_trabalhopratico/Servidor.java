package pd_trabalhopratico;

import java.rmi.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class LeFicheiroUtilizadores {
    
    List <Utilizador> utilizadores = new ArrayList<>();
    
    public LeFicheiroUtilizadores(){
        
        String utilizador;
        URL url = getClass().getResource("Utilizadores.txt");
        File ficheiro_utilizadores = new File(url.getPath());
        
        if(ficheiro_utilizadores.exists())
        {
            try {
                FileReader fr = new FileReader(ficheiro_utilizadores);
                BufferedReader br = new BufferedReader(fr);
                while(true)
                {
                    utilizador = br.readLine();
                    if(utilizador == null)
                        break;
                    utilizadores.add(new Utilizador(utilizador.split(" ")));      
                }
            } catch (IOException ex) {
                System.out.println("Não consegue ler conteudo do ficheiro!");
            }
        }
        else{
            System.out.println("Ficheiro não existe");
        }
    }
    public boolean VerificaUser(InformacaoCliente  x)
    {
        System.out.println(""+x.getInet()+":"+x.getPort()+"-> Pedido de login : "+x.getNome()+" "+x.getPass());
        if(x.getNome() == null && x.getPass() == null)
            return false;
        
        else
        {
            for(int i = 0; i< this.utilizadores.size();i++)
            {
                if(x.getNome().endsWith(utilizadores.get(i).getNome()) && x.getPass().equals(utilizadores.get(i).getPass()))
                    return true;
            }
        }
        return false;
    }
    @Override
    public String toString()
    {
       String aux="";
       for(int i =0; i<this.utilizadores.size();i++)
       {
           aux+="Username: "+utilizadores.get(i).getNome()+" || Password: "+utilizadores.get(i).getPass()+"\n";
       }
       return aux;
    }
}
 
class EsperaCliente extends Thread
{
    ListadeRepositorios listarepositorios;
    int port;
    ServerSocket socket_servidor = null;
    Socket socketCliente = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    AtendeCliente atende_cliente = null;
    GetRemoteClienteInfServiceInterface informacaotoServico;
    
    public EsperaCliente(int port1,ListadeRepositorios x,GetRemoteClienteInfServiceInterface informacaotoServicox)
    {
        listarepositorios = x;
        informacaotoServico = informacaotoServicox;
        port = port1;
        try {
            socket_servidor = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("Erro ao registar ServerSocket -> " + ex.getMessage());
        }
    }
    
    @Override
    public void run() {
        while(true) {
            try{
                while(true) {
                    socketCliente = socket_servidor.accept();
                    System.out.println(socketCliente.getInetAddress()+":"+socketCliente.getPort()+" conectou-se ao servidor");
                    out = new ObjectOutputStream(socketCliente.getOutputStream());
                    in = new ObjectInputStream(socketCliente.getInputStream());
                    LeFicheiroUtilizadores listaUtilizadoresRegistados = new LeFicheiroUtilizadores();
                    atende_cliente = new AtendeCliente(out, in, socketCliente,listaUtilizadoresRegistados,listarepositorios, informacaotoServico);
                    atende_cliente.setDaemon(true);
                    atende_cliente.start();
                }
            } 
            catch (IOException ex) {
                System.out.println("Erro aceitar cliente: "+ex.getMessage());
            }
        }
    } 
}
class VerSaida extends Thread
{
    GetRemoteClienteInfServiceInterface informacaotoServico;
    ListadeRepositorios listarepositorios;
    ArrayList <InformacaoRepositorio> repositorios;
    public VerSaida(ListadeRepositorios x, GetRemoteClienteInfServiceInterface informacaotoServicox)
    {
        informacaotoServico = informacaotoServicox;
        listarepositorios = x;
    }
    @Override
    public void run()
    {
        ArrayList <InformacaoRepositorio> repositorios = listarepositorios.getListaRepositorios();
        do
        {
            try {
                sleep(3000);
                repositorios = listarepositorios.getListaRepositorios();
            } catch (InterruptedException ex) {
                Logger.getLogger(VerSaida.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(repositorios != null)
            {
                for(int i = 0; i< repositorios.size();i++)
                {
                    if(!repositorios.get(i).getT().isAlive())
                    {
                        System.out.println(repositorios.get(i)+" -> Deixou ligação");
                        try {
                            informacaotoServico.DeleteListaFicheiros(new File(repositorios.get(i).getCaminho()).listFiles());
                            informacaotoServico.removeRepositorio(repositorios.get(i).getEndereco(),repositorios.get(i).getPort_id());
                        } catch (RemoteException ex) {
                            Logger.getLogger(VerSaida.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        listarepositorios.deleteRepositorio(repositorios.get(i));
                    }
                }
            }
        }while(true);
    }
}
class EsperaRepositorio extends Thread {
    GetRemoteClienteInfServiceInterface informacaotoServico;
    ListadeRepositorios listarepositorios;
    int port;
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    AtendeCliente atende = null;
    VerSaida verificasaida = null;
    
    public EsperaRepositorio (int port1,ListadeRepositorios x,GetRemoteClienteInfServiceInterface informacaotoServicox)
    {
        informacaotoServico=informacaotoServicox;
        verificasaida = new VerSaida(x,informacaotoServico);
        listarepositorios = x;
        port = port1;
        try {
            socket = new DatagramSocket(port);
        } catch (IOException ex) {
            System.out.println("Erro ao registar ServerSocket -> "+ex.getMessage());
        }
    }
    public void AdicionaRepositorio(InetAddress add, int port,Informacao_a_Passar_ao_Servidor  inf_repo)
    {
        ArrayList <InformacaoRepositorio> repositorios = listarepositorios.getListaRepositorios();
        String nome = new String("Repositorio");
        if(repositorios.isEmpty())
        {
            nome.concat(":" + port);
            repositorios.add(new InformacaoRepositorio(new VerificaTempo(),nome, port, add,inf_repo));
            try {
                informacaotoServico.SetRepositorio(add, port);
            } catch (RemoteException ex) {
                Logger.getLogger(EsperaRepositorio.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(repositorios.get(0)+" -> Ligou-se ao Servidor");
            repositorios.get(0).setT(new VerificaTempo());
            repositorios.get(0).getT().setDaemon(true);
            repositorios.get(0).getT().start();
            listarepositorios.setListaRepositorios(repositorios);
        }
        else
        {
            for(int i=0; i< repositorios.size();i++)
            {
                if(add.getHostAddress().equalsIgnoreCase(repositorios.get(i).getAdd().getHostName()))
                {
                    if(port == repositorios.get(i).getPort_id())
                    {
                        if(repositorios.get(i).getT().isAlive())
                        {
                            repositorios.get(i).setT(new VerificaTempo());
                            repositorios.get(i).getT().setDaemon(true);
                            repositorios.get(i).getT().start();
                            listarepositorios.setListaRepositorios(repositorios);
                            return;
                        }
                        else return;
                    }
                }
            }
            nome.concat(":"+port);
            repositorios.add(new InformacaoRepositorio(new VerificaTempo(),nome, port, add,inf_repo));
            try {
                informacaotoServico.SetRepositorio(add, port);
            } catch (RemoteException ex) {
                Logger.getLogger(EsperaRepositorio.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(repositorios.get(repositorios.size()-1)+" -> Ligou-se ao Servidor");
            repositorios.get(repositorios.size()-1).setT(new VerificaTempo());
            repositorios.get(repositorios.size()-1).getT().setDaemon(true);
            repositorios.get(repositorios.size()-1).getT().start();
            listarepositorios.setListaRepositorios(repositorios);
        }
    }
    public void actualiza_repositorio(InetAddress add, int port,Informacao_a_Passar_ao_Servidor  inf_repo)
    {
        ArrayList <InformacaoRepositorio> repositorios = listarepositorios.getListaRepositorios();
        for(int i=0; i< repositorios.size();i++)
            {
                if(add.getHostAddress().equalsIgnoreCase(repositorios.get(i).getAdd().getHostName()))
                {
                    if(port == repositorios.get(i).getPort_id())
                    {
                        if(repositorios.get(i).getT().isAlive())
                        {
                            repositorios.get(i).setListadeFicheiros(inf_repo.getListadeFicheiros());
                            repositorios.get(i).setNumero_de_ligacoes(inf_repo.getNumerodeLigacoes());
                            repositorios.get(i).setT(new VerificaTempo());
                            repositorios.get(i).getT().setDaemon(true);
                            repositorios.get(i).getT().start();
                            listarepositorios.setListaRepositorios(repositorios);
                            return;
                        }
                        else return;
                    }
                }
            }
    }
    
    @Override
    public void run()
    {     
        verificasaida.setDaemon(true);
        verificasaida.start();
        ByteArrayOutputStream bout;
        ObjectOutputStream oout;
        while(true)
        {
            try 
            {
                while(true)
                {
                    packet = new DatagramPacket(new byte[1024],1024);
                    socket.receive(packet);
                    ByteArrayInputStream in = new ByteArrayInputStream(packet.getData(),0,packet.getLength());
                    ObjectInputStream oin = new ObjectInputStream(in);
                    Informacao_a_Passar_ao_Servidor informacao_do_repositorio = (Informacao_a_Passar_ao_Servidor)oin.readObject();
                    if(informacao_do_repositorio.getComando().equalsIgnoreCase("Informacao"))
                    {
                        this.AdicionaRepositorio(packet.getAddress(),packet.getPort(), informacao_do_repositorio);
                    }
                    if(informacao_do_repositorio.getComando().equalsIgnoreCase("actualizacao"))
                    {
                       this.actualiza_repositorio(packet.getAddress(),packet.getPort(), informacao_do_repositorio);
                    }
                    if(informacao_do_repositorio.getComando().equalsIgnoreCase("apagar"))
                    {
                        InformacaoRepositorio infx = this.listarepositorios.VerMaisLivreComFicheiro(informacao_do_repositorio.getArgumentos_comando());
                        InformacaoEnviarCliente informacaoToRepositorio= new InformacaoEnviarCliente();
                        if(infx== null)
                        {
                            informacaoToRepositorio.setComando("check");
                            bout = new ByteArrayOutputStream();
                            oout = new ObjectOutputStream(bout);
                            oout.writeObject(informacaoToRepositorio);
                            oout.flush();
                            packet.setData(bout.toByteArray());
                            packet.setLength(bout.toByteArray().length);
                            socket.send(packet);
                        }
                        else
                        {
                            informacaoToRepositorio.setComando("passa");
                            informacaoToRepositorio.setPortodeEscutaTCP(infx.getPorto_escuta_tcp());
                            informacaoToRepositorio.setAddress(infx.getAdd());
                            bout = new ByteArrayOutputStream();
                            oout = new ObjectOutputStream(bout);
                            oout.writeObject(informacaoToRepositorio);
                            oout.flush();
                            packet.setData(bout.toByteArray());
                            packet.setLength(bout.toByteArray().length);
                            socket.send(packet);
                        }
                    }
                    if(informacao_do_repositorio.getComando().equalsIgnoreCase("passar_ficheiro"))
                    {
                        InformacaoRepositorio infx = this.listarepositorios.VerMaisLivreSemFicheiro(informacao_do_repositorio.getArgumentos_comando());
                        InformacaoEnviarCliente informacaoToRepositorio= new InformacaoEnviarCliente();
                        if(infx== null)
                        {
                            informacaoToRepositorio.setComando("check");
                            bout = new ByteArrayOutputStream();
                            oout = new ObjectOutputStream(bout);
                            oout.writeObject(informacaoToRepositorio);
                            oout.flush();
                            packet.setData(bout.toByteArray());
                            packet.setLength(bout.toByteArray().length);
                            socket.send(packet);
                        }
                        else
                        {
                            informacaoToRepositorio.setComando("passa");
                            informacaoToRepositorio.setPortodeEscutaTCP(infx.getPorto_escuta_tcp());
                            informacaoToRepositorio.setAddress(infx.getAdd());
                            bout = new ByteArrayOutputStream();
                            oout = new ObjectOutputStream(bout);
                            oout.writeObject(informacaoToRepositorio);
                            oout.flush();
                            packet.setData(bout.toByteArray());
                            packet.setLength(bout.toByteArray().length);
                            socket.send(packet);
                        }
                    }
                }
            } 
            catch (IOException ex) 
            {
                System.out.println("Erro aceitar cliente asfsdf sdf: "+ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println("Erro ler a classe " +ex.getMessage());
            }
        }
    } 
}

class VerificaTempo extends Thread
{
    @Override
    public void run()
    {
        try 
        {
             this.sleep(1000);
        } catch (InterruptedException ex) 
        {
            return;
        }
    }
}
class EnviaListaCliente extends Thread
{
    ObjectOutputStream out;
    ListadeRepositorios repositorios;
    public EnviaListaCliente(ListadeRepositorios repositoriosx,ObjectOutputStream outx)
    {
        repositorios = repositoriosx;
        out = outx;
    }
    @Override
    public void run()
    {
        while(true)
        {
            try {
                sleep(10000);
                    InformacaoEnviarCliente informacaoToCliente = new InformacaoEnviarCliente();
                    informacaoToCliente.setComando("lista");
                    informacaoToCliente.setListaficheiro(repositorios.getFicheiros());
                    out.writeObject(informacaoToCliente);
                    out.flush();
            } catch (InterruptedException ex) {
                return;
            } catch (IOException ex) {
                Logger.getLogger(EnviaListaCliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
class AtendeCliente extends Thread
{
    EnviaListaCliente enviaToCliente;
    ListadeRepositorios repositorios;
    InformacaoCliente infcli;
    LeFicheiroUtilizadores utilizador;
    Socket socketCliente;
    ServerSocket socket; 
    ObjectInputStream in;
    ObjectOutputStream out;
    GetRemoteClienteInfServiceInterface informacaotoServico;
    
    public AtendeCliente(ObjectOutputStream outx,ObjectInputStream inx, Socket socketClientex, LeFicheiroUtilizadores x,ListadeRepositorios listax,GetRemoteClienteInfServiceInterface informacaotoServicox)
    {
        informacaotoServico = informacaotoServicox;
        repositorios = listax;
        socketCliente=socketClientex;
        in = inx;
        out = outx;
        infcli = new InformacaoCliente(null, null, socketClientex.getInetAddress(),socketClientex.getPort());
        utilizador = x;
        enviaToCliente = new EnviaListaCliente(repositorios,out);
    }
    public int compara_ficheiros(String pedido)
    {
        File x = new File(pedido);
        String nome = x.getName();
        ArrayList <String> listaficheiros = new ArrayList<>();
        listaficheiros = repositorios.getFicheiros();
        if(repositorios.getListaRepositorios().size() == 0 )
            return 1;
        for(int i = 0; i< listaficheiros.size();i++)
        {
            String [] ficheiro_tamanho = listaficheiros.get(i).split(" ");
            if(ficheiro_tamanho[0].equalsIgnoreCase(nome))
            {
                return 2;
            }
        }
        return 0;
    }
    public String AtendePedido(String pedidoFromCliente)
    {
        String [] separadas = pedidoFromCliente.split(" ");
        InformacaoEnviarCliente informacaoToCliente = new InformacaoEnviarCliente();
        
        if(separadas[0].equals("1"))
        {
            System.out.println(""+infcli+" Escolheu guardar ficheiro "+separadas[1]+" em repositorio");
            try {
            switch(this.compara_ficheiros(separadas[1]))
            {
                case 0:
                    informacaotoServico.SetCliente(1,infcli.getNome(),infcli.getPass(),infcli.getInet(),infcli.getPort(),separadas[1]);
                    informacaoToCliente.setComando("transferir");
                    informacaoToCliente.setArgumentos(separadas[1]);
                    informacaoToCliente.setPortodeEscutaTCP(repositorios.VerMaisLivre().getPorto_escuta_tcp());
                    informacaoToCliente.setAddress(repositorios.VerMaisLivre().getAdd());
                    break;
                case 1:
                    informacaoToCliente.setComando("erro");
                    informacaoToCliente.setArgumentos("Não existem Repositorios activos");
                    break;
                case 2:
                    informacaoToCliente.setComando("erro");
                    informacaoToCliente.setArgumentos("Este ficheiro já existe nos repositorios");
                    break;
            }
            } catch (RemoteException ex) {
                    Logger.getLogger(AtendeCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
            try {
                out.writeObject(informacaoToCliente);
                out.flush();
            } catch (IOException ex) {
                System.out.println("Erro enviar dados"+ex.getMessage());
            }
        }
        if(separadas[0].equals("2"))
        {
            try{
                switch(this.repositorios.ficheiroexiste(separadas[1]))
                {
                    case 0:
                        //Não há repositorios
                        informacaoToCliente.setComando("erro");
                        informacaoToCliente.setArgumentos("Não existem Repositorios activos");
                        break;
                    case 1://existe p ficheiro
                        informacaotoServico.SetCliente(2,infcli.getNome(),infcli.getPass(),infcli.getInet(),infcli.getPort(),separadas[1]);
                        informacaoToCliente.setComando("apagar");
                        informacaoToCliente.setArgumentos(separadas[1]);
                        informacaoToCliente.setPortodeEscutaTCP(repositorios.VerMaisLivreComFicheiro(separadas[1]).getPorto_escuta_tcp());
                        informacaoToCliente.setAddress(repositorios.VerMaisLivreComFicheiro(separadas[1]).getAdd());
                        break;
                    case 2://ficheiro nao existe
                        informacaoToCliente.setComando("erro");
                        informacaoToCliente.setArgumentos("Este ficheiro não existe nos repositorios");
                        break;
                }
            } catch (RemoteException ex) {
                    Logger.getLogger(AtendeCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
            try {
                out.writeObject(informacaoToCliente);
                out.flush();
            } catch (IOException ex) {
                System.out.println("Erro enviar dados"+ex.getMessage());
            }
        }
        if(separadas[0].equals("3"))
        {
            try{
                switch(this.repositorios.ficheiroexiste(separadas[1]))
                {
                    case 0:
                        //Não há repositorios
                        informacaoToCliente.setComando("erro");
                        informacaoToCliente.setArgumentos("Não existem Repositorios activos");
                        break;
                    case 1://existe p ficheiro
                        informacaotoServico.SetCliente(3,infcli.getNome(),infcli.getPass(),infcli.getInet(),infcli.getPort(),separadas[1]);
                        informacaoToCliente.setComando("carregar");
                        informacaoToCliente.setArgumentos(separadas[1]+" "+separadas[2]);
                        informacaoToCliente.setPortodeEscutaTCP(repositorios.VerMaisLivreComFicheiro(separadas[1]).getPorto_escuta_tcp());
                        informacaoToCliente.setAddress(repositorios.VerMaisLivreComFicheiro(separadas[1]).getAdd());
                        break;
                    case 2://ficheiro nao existe
                        informacaoToCliente.setComando("erro");
                        informacaoToCliente.setArgumentos("Este ficheiro não existe nos repositorios");
                        break;
                }
            } catch (RemoteException ex) {
                    Logger.getLogger(AtendeCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }
            try {
                out.writeObject(informacaoToCliente);
                out.flush();
            } catch (IOException ex) {
                System.out.println("Erro enviar dados"+ex.getMessage());
            }
        }
        return null;
    }
    public void run() {
        try {
            String pedidoFromCliente = (String)in.readObject();
            infcli.setNomePass(pedidoFromCliente);
            if(utilizador.VerificaUser(infcli)) {
                    informacaotoServico.SetCliente(0,infcli.getNome(),infcli.getPass(),infcli.getInet(),infcli.getPort(),null);
                    out.writeObject(true);
                    out.flush();
                    System.out.println(infcli+" Estabeleceu Login com sucesso");
                    out.writeObject(infcli);
                    out.flush();
                    enviaToCliente.setDaemon(true);
                    enviaToCliente.start();
                    while(true)
                    {
                        pedidoFromCliente =(String)in.readObject();
                        AtendePedido(pedidoFromCliente);
                    }
                }
        } catch (IOException ex) 
        {
            enviaToCliente.interrupt();
            try {
                informacaotoServico.reMoveCliente(infcli.getNome(),infcli.getPort());
            } catch (RemoteException ex1) {
                Logger.getLogger(AtendeCliente.class.getName()).log(Level.SEVERE, null, ex1);
            }
            System.out.println(infcli+" Saiu da ligação");
        } catch (ClassNotFoundException ex) {
            System.out.println("Erro class não suportada"+ex.getMessage());
        }
    }
    
}
public class Servidor 
{
    public static void main(String args[])
    {
        GetRemoteClienteInfServiceInterface informacaotoServico;
        String objectUrl;
        objectUrl = "rmi://127.0.0.1/GetInformacaoServidor";
        
        try {
            informacaotoServico = (GetRemoteClienteInfServiceInterface)Naming.lookup(objectUrl);
        
            int port = 5003;
            ListadeRepositorios listarepositorios = new ListadeRepositorios();
            
            //Lanca Thread que espera cliente
            EsperaCliente espera_cliente = new EsperaCliente(port, listarepositorios,informacaotoServico);
            espera_cliente.setDaemon(true);
            espera_cliente.start();
            
            //Lanca Thread que espera Repositorio
            EsperaRepositorio espera_repositorio = new EsperaRepositorio(port,listarepositorios,informacaotoServico);
            espera_repositorio.setDaemon(true);
            espera_repositorio.start();
            do{}while(espera_cliente.isAlive());
        
        } catch (NotBoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
}
