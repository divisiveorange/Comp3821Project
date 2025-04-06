A basic image compressor using Huffman encoding.
It huffman encodes sets of 12 bits corresponding to the leading bits of each color channel for each pixel and then huffman encodes the remaining 12 bits for the 100 (found to be a good sweet spot) most common leading bits.

Basic testing on several 12mp photos (raw photos converted to lossless jpgs), showed an on average reduction from 36mb uncompressed to 22.25mb, a 38% reduction in file size.

Instructions

Run 
Java Encoder {fileToEncode} {possibleOtherFileToEncode}
to encode

and 
Java Decoder {fileToDecode} {possibleOtherFileToDecode}
to decode
