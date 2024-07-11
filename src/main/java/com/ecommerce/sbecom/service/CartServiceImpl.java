package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.APIException;
import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Cart;
import com.ecommerce.sbecom.model.CartItem;
import com.ecommerce.sbecom.model.Product;
import com.ecommerce.sbecom.payload.CartDTO;
import com.ecommerce.sbecom.payload.ProductDTO;
import com.ecommerce.sbecom.respositories.CartItemRepository;
import com.ecommerce.sbecom.respositories.CartRepository;
import com.ecommerce.sbecom.respositories.ProductRepository;
import com.ecommerce.sbecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
//       Find existing cart or create one
        Cart cart = createCart();
//       Retrieve product details

        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));
//       Perform validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if(cartItem != null){
            throw new APIException("Product " + product.getProductName() + " already exists in cart");
        }

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

//        Create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

//        Save cart item
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice()*quantity));
        cartRepository.save(cart);
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productStream = cartItems.stream().map(item->{
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());
//        Return updated cart
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> productDTOs = cart.getCartItems().stream().map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class)).toList();
            cartDTO.setProducts(productDTOs);
            return cartDTO;
        }).collect(Collectors.toList());
        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO =  modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(cartItem -> cartItem.getProduct().setQuantity(cartItem.getQuantity()));
        List<ProductDTO> productDTOs = cart.getCartItems().stream().map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class)).toList();
        cartDTO.setProducts(productDTOs);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        Cart userCart = createCart();
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(userCart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(0);
            cartItem = cartItemRepository.save(newCartItem);
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0){
            throw new APIException("The resulting quantity cannot be negative");
        }
        if(newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        }else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + cartItem.getProductPrice()*quantity);
            cartRepository.save(cart);
        }

         cartItemRepository.save(cartItem);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> productDTOs= cart.getCartItems().stream().map(item->{
                ProductDTO productDTO =  modelMapper.map(item.getProduct(), ProductDTO.class);
                productDTO.setQuantity(item.getQuantity());
                return productDTO;
        }).toList();

        cartDTO.setProducts(productDTOs);

        return cartDTO;

    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
           throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice()*cartItem.getQuantity());
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem == null){
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + cartItem.getProductPrice()*cartItem.getQuantity());

        cartItemRepository.save(cartItem);
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }
}
