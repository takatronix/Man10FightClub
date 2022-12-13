package red.man10.fightclub

    import org.bukkit.plugin.java.JavaPlugin
    import red.man10.man10itembank.Command

class Main : JavaPlugin() {
    override fun onEnable() {

        // Plugin startup logic
        getCommand("mfc")!!.setExecutor(Command)
    }

        override fun onDisable() {
        // Plugin shutdown logic

            logger.info("test")
    }
}