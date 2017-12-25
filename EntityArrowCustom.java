package com.AzeS.tv.entity;

import javax.xml.ws.Provider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public  class EntityArrowCustom extends EntityArrow {
	
		private double damage;
		private int knockbackStrength;
		private int xTile;
	    private int yTile;
	    private int zTile;
	    private Block inTile;
	    private int inData;
	    protected boolean inGround;
	    protected int timeInGround;
	    public EntityArrow.PickupStatus pickupStatus;
	    public int arrowShake;
	    public Entity shootingEntity;
	    private int ticksInGround;
	    private int ticksInAir;
		private boolean doBlockCollisions;
	
	public EntityArrowCustom(World worldIn) {
		super(worldIn);
		 this.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
	     this.damage = 2.0D;
	}
	
	protected EntityArrowCustom createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter)
    {
        return new EntityArrowCustom(worldIn, shooter);
    }

	
	public EntityArrowCustom(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}
@Override
protected void onHit(RayTraceResult raytraceResultIn){
	 Entity entity = raytraceResultIn.entityHit;

	 Type typ = raytraceResultIn.typeOfHit.BLOCK;
     if (entity != null)
     {
         float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
         int i = MathHelper.ceiling_double_int((double)f * this.damage);

         if (this.getIsCritical())
         {
             i += this.rand.nextInt(i / 2 + 2);
         }

         DamageSource damagesource;

         if (this.shootingEntity == null)
         {
             damagesource = DamageSource.causeArrowDamage(this, this);
         }
         else
         {
             damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);
         }

         if (this.isBurning() && !(entity instanceof EntityEnderman))
         {
             entity.setFire(5);
         }

         if (entity.attackEntityFrom(damagesource, (float)i))
         {
             if (entity instanceof EntityLivingBase)
             {
                 EntityLivingBase entitylivingbase = (EntityLivingBase)entity;

                 if (!this.worldObj.isRemote)
                 {
                     entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
                 }

                 if (this.knockbackStrength > 0)
                 {
                     float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);

                     if (f1 > 0.0F)
                     {
                         entitylivingbase.addVelocity(this.motionX * (double)this.knockbackStrength * 0.6000000238418579D / (double)f1, 0.1D, this.motionZ * (double)this.knockbackStrength * 0.6000000238418579D / (double)f1);
                     }
                 }

                 if (this.shootingEntity instanceof EntityLivingBase)
                 {
                     EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
                     EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, entitylivingbase);
                 }

                 this.arrowHit(entitylivingbase);

                 if (this.shootingEntity != null && entitylivingbase != this.shootingEntity && entitylivingbase instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP)
                 {
                     ((EntityPlayerMP)this.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
                 }
             }

         	this.playSound(SoundEvents.BLOCK_ANVIL_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

            World world = Minecraft.getMinecraft().theWorld;
            if(typ == Type.BLOCK){
            	IBlockState blockstate = this.worldObj.getBlockState(raytraceResultIn.getBlockPos());
            	if(blockstate.getBlock() instanceof BlockGlass){
            		this.worldObj.setBlockToAir(raytraceResultIn.getBlockPos());
            	}else{
            		super.onHit(raytraceResultIn);
            	}
           }
       }
   }
}
@Override
public void writeEntityToNBT(NBTTagCompound compound)
{
    compound.setInteger("xTile", this.xTile);
    compound.setInteger("yTile", this.yTile);
    compound.setInteger("zTile", this.zTile);
    compound.setShort("life", (short)this.ticksInGround);
    ResourceLocation resourcelocation = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
    compound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
    compound.setByte("inData", (byte)this.inData);
    compound.setByte("shake", (byte)this.arrowShake);
    compound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
    compound.setByte("pickup", (byte)this.pickupStatus.ordinal());
    compound.setDouble("damage", this.damage);
}
@Override
public void readEntityFromNBT(NBTTagCompound compound)
{
    this.xTile = compound.getInteger("xTile");
    this.yTile = compound.getInteger("yTile");
    this.zTile = compound.getInteger("zTile");
    this.ticksInGround = compound.getShort("life");

    if (compound.hasKey("inTile", 8))
    {
        this.inTile = Block.getBlockFromName(compound.getString("inTile"));
    }
    else
    {
        this.inTile = Block.getBlockById(compound.getByte("inTile") & 255);
    }

    this.inData = compound.getByte("inData") & 255;
    this.arrowShake = compound.getByte("shake") & 255;
    this.inGround = compound.getByte("inGround") == 1;

    if (compound.hasKey("damage", 99))
    {
        this.damage = compound.getDouble("damage");
    }

    if (compound.hasKey("pickup", 99))
    {
        this.pickupStatus = EntityArrow.PickupStatus.getByOrdinal(compound.getByte("pickup"));
    }
    else if (compound.hasKey("player", 99))
    {
        this.pickupStatus = compound.getBoolean("player") ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
    }
}
@Override
public void onCollideWithPlayer(EntityPlayer entityIn)
{
    if (!this.worldObj.isRemote && this.inGround && this.arrowShake <= 0)
    {
        boolean flag = this.pickupStatus == EntityArrow.PickupStatus.ALLOWED || this.pickupStatus == EntityArrow.PickupStatus.CREATIVE_ONLY && entityIn.capabilities.isCreativeMode;

        if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED && !entityIn.inventory.addItemStackToInventory(this.getArrowStack()))
        {
            flag = false;
        }

        if (flag)
        {
            this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityIn.onItemPickup(this, 1);
            this.setDead();
        }
    }
}
@Override
public double getDamage()
{
    return this.damage;
}
@Override
public void setDamage(double damageIn)
{
    this.damage = damageIn;
}
@Override
public void setKnockbackStrength(int knockbackStrengthIn)
{
    this.knockbackStrength = knockbackStrengthIn;
}

	public EntityArrowCustom(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}

	


	public float getGravityVelocity() {
		
		return 0.000F;
		
	}

@Override
public void onUpdate()
{
    super.onUpdate();

    if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
    {
        float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
    IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
    Block block = iblockstate.getBlock();

    if (iblockstate.getMaterial() != Material.AIR)
    {
        AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.worldObj, blockpos);

        if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(blockpos).isVecInside(new Vec3d(this.posX, this.posY, this.posZ)))
        {
            this.inGround = true;
        }
    }

    if (this.arrowShake > 0)
    {
        --this.arrowShake;
    }

    if (this.inGround)
    {
        int j = block.getMetaFromState(iblockstate);

        if (block == this.inTile && j == this.inData)
        {
            ++this.ticksInGround;

            if (this.ticksInGround >= 1200)
            {
                this.setDead();
            }
        }
        else
        {
            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        }

        ++this.timeInGround;
    }
    else
    {
        this.timeInGround = 0;
        ++this.ticksInAir;
        Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult raytraceresult = this.worldObj.rayTraceBlocks(vec3d1, vec3d, false, true, false);
        vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
        vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        if (raytraceresult != null)
        {
            vec3d = new Vec3d(raytraceresult.hitVec.xCoord, raytraceresult.hitVec.yCoord, raytraceresult.hitVec.zCoord);
        }

        Entity entity = this.findEntityOnPath(vec3d1, vec3d);

        if (entity != null)
        {
            raytraceresult = new RayTraceResult(entity);
        }

        if (raytraceresult != null && raytraceresult.entityHit != null && raytraceresult.entityHit instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)raytraceresult.entityHit;

            if (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityplayer))
            {
                raytraceresult = null;
            }
        }

        if (raytraceresult != null)
        {
            this.onHit(raytraceresult);
        }

        if (this.getIsCritical())
        {
            
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f4 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

        for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f4) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float f1 = 0.99F;
        float f2 = 0.05F;

        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
            {
                float f3 = 0.25F;
                this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ, new int[0]);
            }

            f1 = 0.6F;
        }

        if (this.isWet())
        {
            this.extinguish();
        }

        this.motionX *= (double)f1;
        this.motionY *= (double)f1;
        this.motionZ *= (double)f1;

        if (!this.hasNoGravity())
        {
            this.motionY -= 0.05000000074505806D;
        }

        this.setPosition(this.posX, this.posY, this.posZ);
        this.doBlockCollisions();
   
       
        }
    }


	
	protected ItemStack getArrowStack() {
		
		return null;
	}
	
	protected float getVelocity() {
		return 500.0F;
	}
	


	public void prevDistanceWalkedModified(float f) {
		
		
	}
	
	
	public void Impack(RayTraceResult resu){
		  Block block = worldObj.getBlockState(getPosition()).getBlock();
          World world = Minecraft.getMinecraft().theWorld;
          
	this.setDead();
		
		if ( this.inGround){
			this.setDead();
		}
		if(this.inWater){
			this.setDead();
		}
			
        		  if(block == Blocks.GLASS || block == Blocks.GLASS_PANE){
        			  world.destroyBlock(getPosition(), false);
        		  }
        		  if(this.isCollided ){
                		  if(block == Blocks.GLASS || block == Blocks.GLASS_PANE){
                			  world.destroyBlock(getPosition(), false);
                		  }  
        		  }
        		  
        		  
	}
	
	
}