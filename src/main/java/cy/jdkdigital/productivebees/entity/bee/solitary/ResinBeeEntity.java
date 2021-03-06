package cy.jdkdigital.productivebees.entity.bee.solitary;

import cy.jdkdigital.productivebees.entity.bee.SolitaryBeeEntity;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.util.BeeAttributes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.world.World;

public class ResinBeeEntity extends SolitaryBeeEntity
{
    public ResinBeeEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
        beeAttributes.put(BeeAttributes.NESTING_PREFERENCE, ModTags.WOOD_NESTS);
        beeAttributes.put(BeeAttributes.FOOD_SOURCE, ModTags.FOREST_FLOWERS);
    }
}
