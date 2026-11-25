package task2;

/** «Юридичні особи»: рух по прямій до випадкової точки лівої верхньої чверті [0,0]–[w/2,h/2]. */
public class LegalEntityAI extends BaseAI {
    static final double V = 2.0;        // швидкість, пікселів за крок

    LegalEntityAI(World world, Runnable onUpdate) {
        super("Юридичні особи", Entity.Kind.LEGAL, world, onUpdate);
    }

    @Override
    protected void initEntity(Entity e) {
        int w = world.width, h = world.height;
        if (e.x <= w / 2.0 && e.y <= h / 2.0) {      // одразу в цільовій чверті — стоїть
            e.moving = false;
            e.tx = e.x;
            e.ty = e.y;
        } else {
            e.tx = 10 + world.random.nextInt(Math.max(1, w / 2 - 20));
            e.ty = 10 + world.random.nextInt(Math.max(1, h / 2 - 20));
            e.moving = true;
        }
    }

    @Override
    protected void move(Entity e) {
        if (stepTowards(e, V)) {
            e.moving = false;                        // досягла цілі — залишається нерухомою
        }
    }
}
