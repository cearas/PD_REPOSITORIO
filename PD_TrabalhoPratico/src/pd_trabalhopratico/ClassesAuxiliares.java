package pd_trabalhopratico;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.io.Serializable;
import java.util.ArrayList;

public class ClassesAuxiliares 
{
    public void Apresenta_menu_cliente()
    {
        System.out.println("Opções disponiveis : ");
        System.out.println("1 -> Guardar ficheiro em repositorio");
        System.out.println("2 -> Eliminar ficheiro");
        System.out.println("3 -> Transferir ficheiro do repositório");
    }
}
class InformacaodoClientenoServico implements Serializable
{
    private String nome;
    private String pass;
    private String filename;
    private int estado;
    private int port;
    private InetAddress add;
    
    public InformacaodoClientenoServico(String nomex,String passx, String filenamex, int estadox,int portx, InetAddress addx)
    {
     this.add=addx;
     this.estado =estadox;
     this.port = portx;
     this.pass = passx;
     this.nome= nomex;
     this.filename = filenamex;
    }
    @Override
    public String toString()
    {
       String aux = "";
       aux="Cliente "+this.nome+" -> "+this.pass+" com endereco "+this.add+":"+this.port+" encontra-se ";
       switch(this.estado)
       {
           case 0:
               aux += "inactivo";
               break;
           case 1:
               aux+="transferir ficheiro "+this.filename;
               break;
            case 2:
               aux+="apagar ficheiro "+this.filename;
               break;
            case 3:
               aux+="carregar ficheiro "+this.filename;
               break;
       }
       return aux;
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAdd() {
        return add;
    }

    public void setAdd(InetAddress add) {
        this.add = add;
    }
}
class Metodos_Auxiliares_do_Repositorio
{
    
    public File[] carregarFiles(String caminho)
    {
        File x = new File(caminho);
        return x.listFiles();
    }
    public boolean paginaExiste(String caminho)
    {
        File pasta = new File(caminho);
        if(!pasta.isDirectory())
        {
            System.out.println("Não existe esta Pasta");
            return false;
        }
        if(!pasta.canRead() || !pasta.canWrite())
        {
            System.out.println("Pasta não pode Ler/Escrever");
            return false;
        }
        return true;
    }
    public File [] actualiza_ficheiros_repositorio(String PastaFicheiros)
    {
        File pastadorepositorio = new File(PastaFicheiros.trim());
        if(pastadorepositorio.exists())
        {
            if(pastadorepositorio.isDirectory())
            {
                File [] ficheiros = pastadorepositorio.listFiles();
                return ficheiros;
                }
            else
            {
                System.out.println("Caminho disponibilizado não é uma directoria");
                return null;
            }
        }
        else
        {
            System.out.println("Erro ao aceder á pasta do repositório");
            return null;
        }
    }
}
class Utilizador {
    private String nome;
    private String pass;
    
    public Utilizador(String[] login) {
        nome = login[0];
        pass = login[1];
    }
    public String getNome() {
        return nome;
    }
    public String getPass() {
        return pass;
    }
}

class InformacaoCliente implements Serializable{
    
    private String nome;
    private String pass;
    private InetAddress endereco;
    private int port;

    public InformacaoCliente(String nome,String pass, InetAddress endereco, int port){
        this.setNome(nome);
        this.setInet(endereco);
        this.setPort(port);
        this.setPass(pass);
    }
    public void setNomePass(String x){
        String[] login = x.split(" ");
        nome= login[0];
        pass = login[1];  
    }
    public String toString(){
        return ""+this.getNome()+""+this.getInet()+":"+this.getPort()+"-> ";
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public InetAddress getInet() {
        return endereco;
    }
    public void setInet(InetAddress inet) {
        this.endereco = inet;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getPass() {
        return pass;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }
}
class InformacaoEnviarCliente implements Serializable
{
    private String comando;
    private String argumentos;
    private ArrayList <String> listaficheiro;
    private int PortodeEscutaTCP;
    private InetAddress address;
    
    public InformacaoEnviarCliente()
    {
        listaficheiro = new ArrayList<>();
    }

    public String getComando() {
        return comando;
    }

    public void setComando(String comando) {
        this.comando = comando;
    }

    public ArrayList <String> getListaficheiro() {
        return listaficheiro;
    }

    public void setListaficheiro(ArrayList <String> listaficheiro) {
        this.listaficheiro = listaficheiro;
    }

    public String getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(String argumentos) {
        this.argumentos = argumentos;
    }

    public int getPortodeEscutaTCP() {
        return PortodeEscutaTCP;
    }

    public void setPortodeEscutaTCP(int PortodeEscutaTCP) {
        this.PortodeEscutaTCP = PortodeEscutaTCP;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
    
}
class InformacaoRepositorio
{
    private VerificaTempo tempo;
    private ArrayList <String> ListadeFicheiros;
    private String caminho;
    private String nome;
    private File[] ficheiros;
    private int porto_id,porto_escuta_tcp,numero_de_ligacoes;
    private InetAddress endereco;
    
    public InformacaoRepositorio(VerificaTempo tempo, String nome_repositorio, int port, InetAddress endereco_repositorio,Informacao_a_Passar_ao_Servidor informacao_repo)
    {
        this.setT(tempo);
        this.setAdd(endereco_repositorio);
        this.setNome(nome_repositorio);
        this.setPorto_id(port);
        this.setNumero_de_ligacoes(informacao_repo.getNumerodeLigacoes());
        this.setPorto_escuta_tcp(informacao_repo.getPort_escuta_TCP());
        this.setListadeFicheiros(informacao_repo.getListadeFicheiros());
        this.setCaminho(informacao_repo.getPasta_Repositorio());
    }
    public boolean VerFicheiro(String nome)
    {
        for(int i = 0; i< ListadeFicheiros.size();i++)
        {
            String[] nome_part = ListadeFicheiros.get(i).split(" ");
            if(nome_part[0].equalsIgnoreCase(nome))
                return true;
        }
        return false;
    }
    public VerificaTempo getT() {
        return tempo;
    }

    public void setT(VerificaTempo tempo) {
        this.tempo = tempo;
    }

    public String getNome() {
        return nome;
    }

    public  void setNome(String nome) {
        this.nome = nome;
    }

    public  int getPort_id() {
        return getPorto_id();
    }

    public void setPorto_id(int port) {
        this.porto_id = port;
    }

    public  InetAddress getAdd() {
        return getEndereco();
    }

    public  void setAdd(InetAddress add) {
        this.setEndereco(add);
    }
    @Override
    public  String toString()
    {
        return ""+nome+" -> "+getEndereco()+":"+porto_id;
    }

    public ArrayList <String> getListadeFicheiros() {
        return ListadeFicheiros;
    }

    public void setListadeFicheiros(ArrayList <String> ListadeFicheiros) {
        this.ListadeFicheiros = ListadeFicheiros;
    }

    public int getPorto_id() {
        return porto_id;
    }

    public int getPorto_escuta_tcp() {
        return porto_escuta_tcp;
    }

    public void setPorto_escuta_tcp(int porto_escuta_tcp) {
        this.porto_escuta_tcp = porto_escuta_tcp;
    }

    public int getNumero_de_ligacoes() {
        return numero_de_ligacoes;
    }

    public void setNumero_de_ligacoes(int numero_de_ligacoes) {
        this.numero_de_ligacoes = numero_de_ligacoes;
    }

    public InetAddress getEndereco() {
        return endereco;
    }

    public void setEndereco(InetAddress endereco) {
        this.endereco = endereco;
    }

    public File[] getFicheiros() {
        return ficheiros;
    }

    public void setFicheiros(File[] ficheiros) {
        this.ficheiros = ficheiros;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }
}
class InformacaoFicheiro implements Serializable
{
    private String nome;
    private ArrayList <String> caminhos;
    private String tamanho;
    private int n_replicas;
    private String datadeCriacao;
    private String data_de_modificacao;
    public InformacaoFicheiro(String nomex, String caminhox, String tamanhox,int n_replicasx,String dataCriacaox, String data_mod)
    {
        caminhos = new ArrayList<>();
        this.setNome(nomex);
        this.setCaminhos(caminhox);
        n_replicas=1;
        this.setData_de_modificacao(data_mod);
        this.setDatadeCriacao(dataCriacaox);
        this.setTamanho(tamanhox);
    }
    @Override
    public String toString()
    {
        return "Ficheiro -> "+this.getNome()+" "+this.getTamanho()+"||Data criacao/modificao -> "+this.getDatadeCriacao()+"/"+this.getData_de_modificacao()+"|| tem "+this.getN_replicas()+" Replicas";
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public boolean jaExisteCaminho(String caminho)
    {
        for(int i = 0; i< caminhos.size();i++)
        {
            if(caminhos.get(i).equalsIgnoreCase(caminho))
            return true;
        }
        return false;
    }
    public String getCaminho(int i ) {
        return caminhos.get(i);
    }
    public void DeleteCaminho(String caminho)
    {
        caminhos.remove(caminho);
    }
    public void setCaminhos(String caminho) {
        if(caminhos.size() == 0)
        {
            caminhos.add(caminho);
        }
        if(!jaExisteCaminho(caminho))
            caminhos.add(caminho);
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public int getN_replicas() {
        return n_replicas;
    }

    public void setN_replicas(int n_replicas) {
        this.n_replicas = n_replicas;
    }

    public String getDatadeCriacao() {
        return datadeCriacao;
    }

    public void setDatadeCriacao(String datadeCriacao) {
        this.datadeCriacao = datadeCriacao;
    }

    public String getData_de_modificacao() {
        return data_de_modificacao;
    }

    public void setData_de_modificacao(String data_de_modificacao) {
        this.data_de_modificacao = data_de_modificacao;
    }
    
}
class ListadeRepositorios
{
    ArrayList <InformacaoRepositorio> repositorios;
    
    public ListadeRepositorios()
    {
        repositorios = new ArrayList<>();
    }
    public synchronized File[]getTodosFicheiros()
    {
        int cont=0;
        int n_ficheiros = 0;
        for(int i = 0; i< repositorios.size();i++)
        {
            n_ficheiros += repositorios.get(i).getFicheiros().length;
        }
        File[] fx = new File[n_ficheiros];
        for(int i = 0; i< repositorios.size();i++)
        {
            for(int x = 0; x < repositorios.get(i).getFicheiros().length; x++)
            {
                fx[cont] = repositorios.get(i).getFicheiros()[x];
                cont++;
            }
        }
        return fx;
    }
    public synchronized int ficheiroexiste(String nome_ficheiro)
    {
        File ficheiro = new File(nome_ficheiro);
        if(repositorios.size() == 0)
        {
            return 0;
        }
        for(int i = 0; i< repositorios.size();i++)
         {
             if(repositorios.get(i).VerFicheiro(ficheiro.getName()))
             {
                return 1; 
             }
         }
        return 2;
    }
    public synchronized InformacaoRepositorio VerMaisLivreComFicheiro(String nome_ficheiro)
    {
        File ficheiro = new File(nome_ficheiro);
        InformacaoRepositorio paraRetornar = null;
        for(int i = 0; i< repositorios.size();i++)
        {
            if(repositorios.get(i).VerFicheiro(ficheiro.getName()))
            {
                if(paraRetornar == null)
                    paraRetornar=repositorios.get(i);
                else
                {
                   if(paraRetornar.getNumero_de_ligacoes() > repositorios.get(i).getNumero_de_ligacoes())
                    {
                        paraRetornar=repositorios.get(i);
                    } 
                }
            }
        }
        return paraRetornar;
    }
    public synchronized InformacaoRepositorio VerMaisLivreSemFicheiro(String nome_ficheiro)
    {
        File ficheiro = new File(nome_ficheiro);
        InformacaoRepositorio paraRetornar = null;
        if(repositorios.size() == 1)
        {
            return null;
        }
        for(int i = 0; i< repositorios.size();i++)
        {
            if(!repositorios.get(i).VerFicheiro(ficheiro.getName()))
            {
                if(paraRetornar == null)
                    paraRetornar=repositorios.get(i);
                else
                {
                   if(paraRetornar.getNumero_de_ligacoes() > repositorios.get(i).getNumero_de_ligacoes())
                    {
                        paraRetornar=repositorios.get(i);
                    } 
                }
            }
        }
        return paraRetornar;
    }
    public synchronized InformacaoRepositorio VerMaisLivre()
    {
        InformacaoRepositorio paraRetornar = null;
        for(int i = 0; i< repositorios.size();i++)
        {
            if(i== 0)
            {
                paraRetornar=repositorios.get(i);
            }
            else
            {
                if(paraRetornar.getNumero_de_ligacoes() > repositorios.get(i).getNumero_de_ligacoes())
                {
                    paraRetornar=repositorios.get(i);
                }
            }
        }
        return paraRetornar;
    }
    public synchronized  ArrayList <InformacaoRepositorio> getListaRepositorios()
    {
        return repositorios;
    }
    public synchronized void addRepositorio(InformacaoRepositorio x)
    {
        repositorios.add(x);
    }
    public synchronized  void deleteRepositorio(InformacaoRepositorio x)
    {
        for(int i=0; i< this.repositorios.size(); i++)
        {
            if(x.getEndereco() == repositorios.get(i).getAdd())
            {
                if(x.getPorto_id() == repositorios.get(i).getPorto_id())
                {
                    repositorios.remove(i);
                }
            }
        }
    }
    public synchronized  void setListaRepositorios( ArrayList <InformacaoRepositorio> x)
    {
        repositorios = new ArrayList<>(x);
    }
    public synchronized ArrayList<String> getFicheiros()
    {
        ArrayList <String> aux = new ArrayList<>();
        boolean teste=false;
        for(int i = 0; i< repositorios.size();i++)
        {
            for(int z = 0 ; z < repositorios.get(i).getListadeFicheiros().size();z++)
            {
                if(aux.size() == 0)
                {
                    aux.add(repositorios.get(i).getListadeFicheiros().get(z));
                    teste = true;
                }
                else
                {
                    for(int x = 0; x < aux.size();x++)
                    {
                        if(aux.get(x).equalsIgnoreCase(repositorios.get(i).getListadeFicheiros().get(z)))
                        {
                            teste = true;
                        }
                    }
                }
                if(teste)
                {
                    teste = false;
                }
                else
                {
                    aux.add(repositorios.get(i).getListadeFicheiros().get(z));
                }
            }
        }
        return aux;
    }
    
}
class InformacaoServidor {
    private Socket socket;
    private InetAddress Address;
    private int port;
    
    public InformacaoServidor(InetAddress endereco_servidor,int porta, Socket x){
        this.setSokcet(x);
        this.setAddress(endereco_servidor);
        this.setPort(porta);
    }
    public InetAddress getAddress() {
        return Address;
    }
    public void setAddress(InetAddress Address) {
        this.Address = Address;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public Socket getSokcet() {
        return socket;
    }
    public void setSokcet(Socket sokcet) {
        this.socket = sokcet;
    }
}

class Informacao_a_Passar_ao_Servidor implements Serializable {
    private String pasta_Repositorio;
    private String comando;
    private String argumentos_comando;
    private int port_escuta_TCP;
    private ArrayList <String> ListadeFicheiros;
    private int NumerodeLigacoes;
    public Informacao_a_Passar_ao_Servidor()
    {
        comando = null;
        argumentos_comando = null;
        port_escuta_TCP = 0;
        ListadeFicheiros = new ArrayList<>();
        NumerodeLigacoes = 0;
    }

    public int getPort_escuta_TCP() {
        return port_escuta_TCP;
    }

    public void setPort_escuta_TCP(int port) {
        this.port_escuta_TCP = port;
    }
    public ArrayList <String> getListadeFicheiros() {
        return ListadeFicheiros;
    }

    public void setListadeFicheiros(ArrayList <String> ListadeFicheiros) 
    {
        this.ListadeFicheiros = ListadeFicheiros;
    }

    public int getNumerodeLigacoes() {
        return NumerodeLigacoes;
    }

    public void setNumerodeLigacoes(int NumerodeLigacoes) {
        this.NumerodeLigacoes = NumerodeLigacoes;
    }

    public String getComando() {
        return comando;
    }

    public void setComando(String comando) {
        this.comando = comando;
    }

    public String getArgumentos_comando() {
        return argumentos_comando;
    }

    public void setArgumentos_comando(String argumentos_comando) {
        this.argumentos_comando = argumentos_comando;
    }

    public String getPasta_Repositorio() {
        return pasta_Repositorio;
    }

    public void setPasta_Repositorio(String pasta_Repositorio) {
        this.pasta_Repositorio = pasta_Repositorio;
    }
    
}

class InformacaoDoRepositorio 
{
    int port;
    InetAddress add;
    ArrayList <String> ListadeFicheiros;
    int NumerodeLigacoes;
    public InformacaoDoRepositorio(int _port, InetAddress _add)
    {
        add = _add;
        port = _port;
        ListadeFicheiros = new ArrayList<>();
        NumerodeLigacoes = 0;
    }
    public synchronized void IncrementaLigacao()
    {
        NumerodeLigacoes++;
    }
    public synchronized void DecrementaLigacao()
    {
        NumerodeLigacoes--;
    }
    public synchronized void ActualizaListaFicheiros(File[]ficheiros)
    { 
                ListadeFicheiros.clear();
                for(int i = 0; i< ficheiros.length; i++)
                {
                    String ficheiro = ""+ficheiros[i].getName()+" -> "+ficheiros[i].length()/1024+"Kb";
                    ListadeFicheiros.add(ficheiro);
                }
            
    }
    public synchronized int getNumeroDeLigacoes()
    {
        return NumerodeLigacoes;
    }
    public synchronized  ArrayList <String> getListaFicheiros()
    {
        return ListadeFicheiros;
    }
}
