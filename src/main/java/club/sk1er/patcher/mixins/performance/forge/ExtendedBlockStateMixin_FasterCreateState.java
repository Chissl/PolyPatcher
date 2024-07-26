package club.sk1er.patcher.mixins.performance.forge;

import club.sk1er.patcher.util.forge.ExtendedStateImplementation;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
//#if MC==10809
import net.minecraft.block.state.BlockState;
//#endif
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ExtendedBlockState.class, remap = false)
public class ExtendedBlockStateMixin_FasterCreateState
    //#if MC==10809
    extends BlockState
    //#endif
{
    //#if MC==10809
    public ExtendedBlockStateMixin_FasterCreateState(Block blockIn, IProperty... properties) {
        super(blockIn, properties);
    }

    /**
     * @author MicrocontrollersDev
     * @reason Faster createState
     */
    @Overwrite
    protected StateImplementation createState(Block block, ImmutableMap<IProperty, Comparable> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        if (unlistedProperties == null || unlistedProperties.isEmpty()) {
            return super.createState(block, properties, unlistedProperties);
        } else {
            return new ExtendedStateImplementation(block, properties, unlistedProperties, null, null);
        }
    }
    //#endif
}
