package task2;

/** «Фізичні особи»: рух по прямій до випадкової точки правої нижньої чверті [w/2,h/2]–[w,h]. */
public class NaturalPersonAI extends BaseAI {
    static final double V = 2.5;        // швидкість, пікселів за крок

    NaturalPersonAI(World world, Runnable onUpdate) {
        super("Фізичні особи", Entity.Kind.NATURAL, world, onUpdate);
    }

    @Override
    protected void initEntity(Entity e) {
        int w = world.width, h = world.height;
        if (e.x >= w / 2.0 && e.y >= h / 2.0) {      // одразу в цільовій чверті — стоїть
            e.moving = false;
            e.tx = e.x;
            e.ty = e.y;
        } else {
            e.tx = w / 2 + 10 + world.random.nextInt(Math.max(1, w / 2 - 20));
            e.ty = h / 2 + 10 + world.random.nextInt(Math.max(1, h / 2 - 20));
            e.moving = true;
        }
    }

    @Override
    protected void move(Entity e) {
        if (stepTowards(e, V)) {
            e.moving = false;
        }
    }
}
