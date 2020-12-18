package br.ufma.gerentepass;

public class LocalizacaoPass {
    String identificador;
    Double latitude;
    Double longitude;
    String altitude;
    String velocidade;
    String date;
    Double latitudeDestino;
    Double longitudeDestino;

    //String teste = '' ;

    LocalizacaoPass(){
        latitude = -2.532165;
        longitude = -44.296644;
        latitudeDestino = -2.53456;
        longitudeDestino = -44.29345;
    }

    public String getIdentificador() {
        return identificador;
    }
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAltitude() {
        return altitude;
    }
    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getVelocidade() {
        return velocidade;
    }
    public void setVelocidade(String velocidade) {
        this.velocidade = velocidade;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getLatitudeDestino() { return  latitudeDestino; }
    public void setLatitudeDestino(Double latitudeDestino) { this.latitudeDestino = latitudeDestino; }

    public Double getLongitudeDestino() { return longitudeDestino; }
    public void setLongitudeDestino(Double longitudeDestino) { this.longitudeDestino = longitudeDestino; }
}
