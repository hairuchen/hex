package me.chr.hex.extend.properties.minio;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: CHR
 * @Date: create in 2026/2/13
 **/
@Data
@ToString
@NoArgsConstructor
public class ChunkMessage {
    private String fileId;
    private String chunk;
    private String chunkId;
    private String nextId;

    public ChunkMessage(String fileId,String chunk,String chunkId,String nextId){
        this.fileId=fileId;
        this.chunk=chunk;
        this.chunkId=chunkId;
        this.nextId=nextId;
    }
}
