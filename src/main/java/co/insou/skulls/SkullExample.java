package co.insou.skulls;

import org.bukkit.inventory.ItemStack;

public class SkullExample {

    private static final String PAUSE_URL = "http://textures.minecraft.net/texture/ec2eb921628e4e38c9d9da39bba577da6dbfe08f10993fec8c8155aaaf976";
    private static final String PLAY_URL = "http://textures.minecraft.net/texture/4ae29422db4047efdb9bac2cdae5a0719eb772fccc88a66d912320b343c341";

    private ItemStack pauseButton;
    private ItemStack playButton;

    public SkullExample() {

    }

    public void loadItems() {
        pauseButton = new SkullMaker().withSkinUrl(PAUSE_URL).build();
        playButton = new SkullMaker().withSkinUrl(PLAY_URL).build();
    }

    public ItemStack getPauseButton() {
        return pauseButton.clone();
    }

    public ItemStack getPlayButton() {
        return playButton.clone();
    }

}
