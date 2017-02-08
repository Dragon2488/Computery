package net.dragon.computery.item;

import net.dragon.computery.block.BlockTrackNFC;

public class Objects {

    public static ItemOpenComputers ocComponents;
    public static CP_MetaGenerated_Item_01 metaItem;
    public static BlockTrackNFC nfcTrack;

    public static void init() {
        ocComponents = new ItemOpenComputers();
        metaItem = new CP_MetaGenerated_Item_01();
        nfcTrack = new BlockTrackNFC();
    }

}
