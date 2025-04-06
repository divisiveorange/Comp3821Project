A basic image compressor using Huffman encoding. It huffman encodes sets of 12 bits corresponding to the leading bits of each color channel for each pixel and then huffman encodes the remaining 12 bits for the 100 (found to be a good sweet spot) most common leading bits.

Basic testing on several 12mp photos (raw photos converted to lossless jpgs), showed an on average reduction from 36mb uncompressed to 22.25mb, a 38% reduction in file size.

Instructions

Run Java Encoder {fileToEncode} {possibleOtherFileToEncode} to encode

and Java Decoder {fileToDecode} {possibleOtherFileToDecode} to decode

Rationale behind decisions:

Java was chosen for this project due to it's ability to easily spin up virtual threads and for easily storing the Huffman table by using Java's serialisation interface.

This compression is designed with the idea that in a photograph, there will be large areas where the pixels are of similar colour but that there will not be many areas where there is the exact same colour. Therefore, we can just take the first 4 bits of each colour channel and compress those 12 bits. This allows the primary huffman table to contain only a maximum 4096 elements. Extra compression can be gained from compressing the last 12 bits of the most common N leading bits. From some testing, it was found that N=100 produced roughly optimal results before the additional space of the Huffman tables outweighted the benefits of the compression.